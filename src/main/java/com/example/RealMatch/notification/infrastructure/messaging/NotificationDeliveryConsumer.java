package com.example.RealMatch.notification.infrastructure.messaging;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.handler.annotation.Header;

import org.springframework.stereotype.Component;

import com.example.RealMatch.notification.application.service.NotificationDeliveryClaimService;
import com.example.RealMatch.notification.application.service.NotificationOutboxService;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.repository.NotificationRepository;
import com.example.RealMatch.notification.infrastructure.sender.NotificationChannelSender;
import com.example.RealMatch.notification.infrastructure.sender.PermanentSendFailureException;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.rabbitmq.client.Channel;

/**
 * MQ Consumer. claim → 발송(FCM/Email) → 결과 기록 순서를 엄격히 따른다.
 * - claim 실패(0 row) → ACK 후 종료 (중복 발송 방지)
 * - 일시 오류(DB 등) → ACK + delivery RETRY + Outbox 재생성 → Recovery가 재발행
 * - 영구 오류(역직렬화, 메시지 형식 오류 등) → NACK(requeue=false) → DLQ
 * - @Transactional 없음 — 외부 API가 TX 안에서 실행되는 것을 방지
 */
@Component
public class NotificationDeliveryConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDeliveryConsumer.class);

    private final NotificationDeliveryClaimService claimService;
    private final NotificationOutboxService outboxService;
    private final NotificationRepository notificationRepository;
    private final Map<NotificationChannel, NotificationChannelSender> senderMap;

    public NotificationDeliveryConsumer(
            NotificationDeliveryClaimService claimService,
            NotificationOutboxService outboxService,
            NotificationRepository notificationRepository,
            List<NotificationChannelSender> senders) {
        this.claimService = claimService;
        this.outboxService = outboxService;
        this.notificationRepository = notificationRepository;
        this.senderMap = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannelSender sender : senders) {
            this.senderMap.put(sender.getChannel(), sender);
        }
        LOG.info("[DeliveryConsumer] Initialized with senders: {}", this.senderMap.keySet());
    }

    @RabbitListener(
            queues = RabbitMqConfig.NOTIFICATION_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleDelivery(NotificationDeliveryMessage message,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        LOG.debug("[DeliveryConsumer] Received message. {}", message);

        try {
            processMessage(message);
            channel.basicAck(deliveryTag, false); // 정상 처리 완료 → ACK
        } catch (Exception e) {
            if (isTransient(e)) {
                handleTransientError(message, channel, deliveryTag, e);
            } else {
                LOG.error("[DeliveryConsumer] Permanent error. Sending to DLQ. message={}, error={}",
                        message, e.getMessage(), e);
                safeNack(channel, deliveryTag);
            }
        }
    }

    /**
     * 일시 오류: ACK하여 메시지 제거 + delivery RETRY 기록 + 재발행용 Outbox 생성.
     * RecoveryScheduler가 RETRY delivery에 대해 Outbox를 만들고 Publisher가 다시 MQ로 보낸다.
     */
    private void handleTransientError(NotificationDeliveryMessage message, Channel channel,
                                      long deliveryTag, Exception e) {
        UUID deliveryId = null;
        UUID notificationId = null;
        NotificationChannel channelEnum = null;
        try {
            deliveryId = UUID.fromString(message.getDeliveryId());
            notificationId = UUID.fromString(message.getNotificationId());
            channelEnum = NotificationChannel.valueOf(message.getChannel());
        } catch (Exception parseEx) {
            // 파싱 실패 = poison(영구 오류). transient가 아님.
            LOG.warn("[DeliveryConsumer] Poison message (cannot parse). Sending to DLQ. message={}",
                    message, parseEx);
            safeNack(channel, deliveryTag);
            return;
        }

        try {
            claimService.recordFailure(deliveryId, e.getMessage());
            outboxService.createRetryOutboxIfAbsent(deliveryId, notificationId, channelEnum);
            channel.basicAck(deliveryTag, false);
            LOG.warn("[DeliveryConsumer] Transient error. Scheduled retry. deliveryId={}, error={}",
                    deliveryId, e.getMessage());
        } catch (Exception retryEx) {
            // DB가 흔들리는 중이면 재시도 기록도 실패함. DLQ로 보내지 않고 ACK → stuck 상태로 두면
            // recoverStuckDeliveries가 10분 후 IN_PROGRESS → RETRY로 복구하여 재발행 경로로 보냄.
            LOG.error("[DeliveryConsumer] Transient but failed to schedule retry. ACK to avoid DLQ pollution. "
                    + "deliveryId={}, originalError={}, scheduleError={}",
                    deliveryId, e.getMessage(), retryEx.getMessage(), retryEx);
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException ackEx) {
                LOG.error("[DeliveryConsumer] Failed to ACK after schedule retry failure. deliveryTag={}",
                        deliveryTag, ackEx);
            }
        }
    }

    /** DB/네트워크 등 일시 오류면 true. ShutdownSignalException은 제외(채널/커넥션 문제는 컨테이너가 재연결). */
    private static boolean isTransient(Throwable e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof DataAccessException) {
                return true;
            }
            if (t instanceof SocketTimeoutException || t instanceof ConnectException || t instanceof UnknownHostException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private void processMessage(NotificationDeliveryMessage message) {
        UUID deliveryId = UUID.fromString(message.getDeliveryId());
        UUID notificationId = UUID.fromString(message.getNotificationId());
        NotificationChannel notificationChannel = NotificationChannel.valueOf(message.getChannel());

        // Step 1: 조건절 UPDATE로 claim (PENDING/RETRY → IN_PROGRESS)
        boolean claimed = claimService.claimDelivery(deliveryId);
        if (!claimed) {
            LOG.debug("[DeliveryConsumer] Already processed. deliveryId={}", deliveryId);
            return;
        }

        // Step 2: 알림 원장 + 발송기 조회
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            claimService.markPermanentlyFailed(deliveryId,
                    "Notification not found: " + notificationId);
            return;
        }

        NotificationChannelSender sender = senderMap.get(notificationChannel);
        if (sender == null || !sender.isAvailable()) {
            claimService.recordFailure(deliveryId,
                    "No available sender for channel: " + notificationChannel);
            LOG.warn("[DeliveryConsumer] No sender for channel={}. deliveryId={}",
                    notificationChannel, deliveryId);
            return;
        }

        // Step 3: 외부 API 발송 (TX 밖) → 결과 기록
        try {
            String providerMessageId = sender.send(notification);
            claimService.markSent(deliveryId, providerMessageId);
            LOG.info("[DeliveryConsumer] Sent. deliveryId={}, channel={}, providerId={}",
                    deliveryId, notificationChannel, providerMessageId);

        } catch (PermanentSendFailureException e) {
            claimService.markPermanentlyFailed(deliveryId, e.getMessage());
            LOG.warn("[DeliveryConsumer] Permanent failure. deliveryId={}, channel={}, reason={}",
                    deliveryId, notificationChannel, e.getMessage());

        } catch (Exception e) {
            claimService.recordFailure(deliveryId, e.getMessage());
            LOG.warn("[DeliveryConsumer] Transient failure. deliveryId={}, channel={}, reason={}",
                    deliveryId, notificationChannel, e.getMessage());
        }
    }

    private void safeNack(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException e) {
            LOG.error("[DeliveryConsumer] Failed to NACK. deliveryTag={}", deliveryTag, e);
        }
    }
}
