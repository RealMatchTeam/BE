package com.example.RealMatch.notification.application.service;

import java.util.Set;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.notification.application.dto.CreateNotificationCommand;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.notification.domain.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.domain.repository.NotificationRepository;
import com.example.RealMatch.notification.exception.NotificationErrorCode;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationChannelResolver channelResolver;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification create(CreateNotificationCommand command) {
        Notification notification = Notification.builder()
                .userId(command.getUserId())
                .kind(command.getKind())
                .title(command.getTitle())
                .body(command.getBody())
                .referenceType(command.getReferenceType())
                .referenceId(command.getReferenceId())
                .campaignId(command.getCampaignId())
                .proposalId(command.getProposalId())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // NotificationKind에 맞는 채널만 PENDING 레코드 생성
        createPendingDeliveries(savedNotification.getId(), command.getKind(), command.getEventId(), command.getUserId());

        return savedNotification;
    }

    private void createPendingDeliveries(UUID notificationId, NotificationKind kind, String eventId, Long receiverId) {
        Set<NotificationChannel> channels = channelResolver.resolveChannels(kind);

        for (NotificationChannel channel : channels) {
            String idempotencyKey = generateIdempotencyKey(eventId, kind, receiverId, channel);
            try {
                NotificationDelivery delivery = NotificationDelivery.builder()
                        .notificationId(notificationId)
                        .channel(channel)
                        .status(DeliveryStatus.PENDING)
                        .idempotencyKey(idempotencyKey)
                        .build();
                notificationDeliveryRepository.save(delivery);
                LOG.debug("[Notification] Created delivery. idempotencyKey={}, channel={}", idempotencyKey, channel);
            } catch (DataIntegrityViolationException e) {
                // unique 제약 충돌 = 이미 생성된 것 (멱등성 보장)
                if (e.getCause() instanceof ConstraintViolationException) {
                    LOG.debug("[Notification] Delivery already exists (idempotent). idempotencyKey={}, channel={}",
                            idempotencyKey, channel);
                } else {
                    throw e; // 다른 데이터 무결성 오류는 재발생
                }
            }
        }
    }

    private String generateIdempotencyKey(String eventId, NotificationKind kind, Long receiverId, NotificationChannel channel) {
        return String.format("%s:%s:%d:%s", eventId, kind, receiverId, channel);
    }

    public void markAsRead(Long userId, UUID notificationId) {
        Notification notification = findNotificationForUser(userId, notificationId);
        notification.markAsRead();
    }

    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    public void softDelete(Long userId, UUID notificationId) {
        Notification notification = findNotificationForUser(userId, notificationId);
        notification.softDelete();
    }

    private Notification findNotificationForUser(Long userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(userId)) {
            throw new CustomException(NotificationErrorCode.NOTIFICATION_FORBIDDEN);
        }

        return notification;
    }
}
