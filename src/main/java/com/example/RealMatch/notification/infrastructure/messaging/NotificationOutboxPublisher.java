package com.example.RealMatch.notification.infrastructure.messaging;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.RealMatch.notification.application.service.NotificationOutboxService;
import com.example.RealMatch.notification.domain.entity.NotificationOutbox;

import lombok.RequiredArgsConstructor;

/**
 * Outbox Publisher. 5초마다 PENDING Outbox를 claim → MQ 발행.
 * - @Transactional 없음 — MQ 발행이 TX 밖에서 실행됨을 구조적으로 보장
 * - DB 연산은 NotificationOutboxService를 통해 메서드별 독립 TX
 */
@Component
@RequiredArgsConstructor
public class NotificationOutboxPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationOutboxPublisher.class);
    private static final int BATCH_SIZE = 50;

    private final NotificationOutboxService outboxService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${notification.publisher.confirm-timeout-ms:5000}")
    private long confirmTimeoutMs = 5000;

    @Scheduled(fixedDelay = 5_000, initialDelay = 5_000)
    public void publishPendingOutbox() {
        List<NotificationOutbox> pendingList = outboxService.findPendingOutbox(BATCH_SIZE);

        if (pendingList.isEmpty()) {
            return;
        }

        LOG.info("[OutboxPublisher] Found {} pending outbox entries.", pendingList.size());

        for (NotificationOutbox outbox : pendingList) {
            publishWithClaimGuard(outbox);
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
    }

    private void publishWithClaimGuard(NotificationOutbox outbox) {
        // PENDING → SENDING claim (조건절 UPDATE)
        boolean claimed = outboxService.claimOutbox(outbox.getId());
        if (!claimed) {
            LOG.debug("[OutboxPublisher] Claim failed (already processing). outboxId={}", outbox.getId());
            return;
        }

        try {
            NotificationDeliveryMessage message = new NotificationDeliveryMessage(
                    outbox.getDeliveryId().toString(),
                    outbox.getNotificationId().toString(),
                    outbox.getChannel().name());

            CorrelationData correlation = new CorrelationData();
            // A publish is successful only after broker confirmation and successful routing.
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.NOTIFICATION_EXCHANGE,
                    RabbitMqConfig.NOTIFICATION_ROUTING_KEY,
                    message, correlation);

            CorrelationData.Confirm confirm = correlation.getFuture().get(confirmTimeoutMs, TimeUnit.MILLISECONDS);
            if (!confirm.isAck() || correlation.getReturned() != null) {
                throw new IllegalStateException("Broker rejected or returned publish: " + confirm.getReason());
            }

            // 성공 → SENDING → SENT
            outboxService.markOutboxSent(outbox.getId());

            LOG.debug("[OutboxPublisher] Published. outboxId={}, deliveryId={}",
                    outbox.getId(), outbox.getDeliveryId());

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            // 실패 → retryCount++ / PENDING 또는 FAILED
            outboxService.markOutboxPublishFailed(outbox.getId(), e.getMessage());
            LOG.error("[OutboxPublisher] Publish failed. outboxId={}, deliveryId={}, error={}",
                    outbox.getId(), outbox.getDeliveryId(), e.getMessage());
        }
    }
}
