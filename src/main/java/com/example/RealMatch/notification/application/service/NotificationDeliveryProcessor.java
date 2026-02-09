package com.example.RealMatch.notification.application.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.domain.repository.NotificationRepository;
import com.example.RealMatch.notification.infrastructure.sender.NotificationChannelSender;
import com.example.RealMatch.notification.infrastructure.sender.PermanentSendFailureException;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

/**
 * 개별 NotificationDelivery 건의 발송을 처리하는 프로세서.
 * 각 delivery를 독립 트랜잭션(REQUIRES_NEW)으로 처리하여 장애를 격리한다.
 */
@Service
public class NotificationDeliveryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDeliveryProcessor.class);

    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationRepository notificationRepository;
    private final Map<NotificationChannel, NotificationChannelSender> senderMap;

    public NotificationDeliveryProcessor(
            NotificationDeliveryRepository deliveryRepository,
            NotificationRepository notificationRepository,
            List<NotificationChannelSender> senders) {
        this.deliveryRepository = deliveryRepository;
        this.notificationRepository = notificationRepository;

        this.senderMap = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannelSender sender : senders) {
            this.senderMap.put(sender.getChannel(), sender);
        }
        LOG.info("[DeliveryProcessor] Initialized with senders: {}", this.senderMap.keySet());
    }

    /**
     * 단건 배달을 처리한다.
     * REQUIRES_NEW 트랜잭션으로 실행되어 실패가 다른 건에 영향을 주지 않는다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDelivery(UUID deliveryId) {
        // 1) 최신 상태로 다시 조회 (stale read 방지)
        NotificationDelivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null) {
            LOG.warn("[DeliveryProcessor] Delivery not found. deliveryId={}", deliveryId);
            return;
        }
        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            LOG.debug("[DeliveryProcessor] Delivery already processed. deliveryId={}, status={}",
                    deliveryId, delivery.getStatus());
            return;
        }

        // 2) IN_PROGRESS로 전환 (워커 점유)
        delivery.markAsInProgress();
        deliveryRepository.saveAndFlush(delivery);

        // 3) 알림 원장 조회
        Notification notification = notificationRepository.findById(delivery.getNotificationId()).orElse(null);
        if (notification == null) {
            delivery.markAsPermanentlyFailed("Notification not found: " + delivery.getNotificationId());
            return;
        }

        // 4) 채널별 발송기 조회
        NotificationChannelSender sender = senderMap.get(delivery.getChannel());
        if (sender == null || !sender.isAvailable()) {
            delivery.recordFailure("No available sender for channel: " + delivery.getChannel());
            LOG.warn("[DeliveryProcessor] No sender for channel={}. deliveryId={}",
                    delivery.getChannel(), deliveryId);
            return;
        }

        // 5) 발송 시도
        try {
            String providerMessageId = sender.send(notification);
            delivery.markAsSent(providerMessageId);
            LOG.debug("[DeliveryProcessor] Delivery sent. deliveryId={}, channel={}, providerId={}",
                    deliveryId, delivery.getChannel(), providerMessageId);
        } catch (PermanentSendFailureException e) {
            // 재시도 불가능 → 영구 실패
            delivery.markAsPermanentlyFailed(e.getMessage());
            LOG.warn("[DeliveryProcessor] Permanent failure. deliveryId={}, channel={}, reason={}",
                    deliveryId, delivery.getChannel(), e.getMessage());
        } catch (Exception e) {
            // 일시적 실패 → 재시도 스케줄링
            delivery.recordFailure(e.getMessage());
            LOG.warn("[DeliveryProcessor] Transient failure. deliveryId={}, channel={}, attempt={}, reason={}",
                    deliveryId, delivery.getChannel(), delivery.getAttemptCount(), e.getMessage());
        }
    }
}
