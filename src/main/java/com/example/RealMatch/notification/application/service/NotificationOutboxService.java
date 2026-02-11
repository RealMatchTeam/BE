package com.example.RealMatch.notification.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.entity.enums.OutboxStatus;
import com.example.RealMatch.notification.domain.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.domain.repository.NotificationOutboxRepository;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

import lombok.RequiredArgsConstructor;

/**
 * Outbox DB 연산의 트랜잭션 경계를 제공한다.
 * 각 메서드가 독립 TX로 실행 → MQ 발행이 TX 밖에서 호출됨을 보장.
 */
@Service
@RequiredArgsConstructor
public class NotificationOutboxService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationOutboxService.class);

    private static final List<DeliveryStatus> RETRYABLE_DELIVERY_STATUSES =
            List.of(DeliveryStatus.PENDING, DeliveryStatus.RETRY);

    private final NotificationOutboxRepository outboxRepository;
    private final NotificationDeliveryRepository deliveryRepository;

    @Transactional(readOnly = true)
    public List<NotificationOutbox> findPendingOutbox(int limit) {
        return outboxRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING,
                PageRequest.of(0, limit));
    }

    /** PENDING → SENDING claim. */
    @Transactional
    public boolean claimOutbox(UUID outboxId) {
        int updated = outboxRepository.claimOutbox(
                outboxId, OutboxStatus.SENDING, OutboxStatus.PENDING);
        return updated > 0;
    }

    /** SENDING → SENT. */
    @Transactional
    public void markOutboxSent(UUID outboxId) {
        int updated = outboxRepository.markAsSent(
                outboxId, OutboxStatus.SENT, OutboxStatus.SENDING);
        if (updated == 0) {
            LOG.warn("[Outbox] Failed to mark SENT. outboxId={}", outboxId);
        }
    }

    /** 발행 실패: retryCount 증가 + status(PENDING/FAILED)를 DB에서 원자적으로 결정. */
    @Transactional
    public void markOutboxPublishFailed(UUID outboxId, String error) {
        int updated = outboxRepository.markPublishFailed(
                outboxId,
                truncate(error, 500),
                NotificationOutbox.MAX_PUBLISH_RETRY,
                OutboxStatus.FAILED,
                OutboxStatus.PENDING,
                List.of(OutboxStatus.SENDING, OutboxStatus.PENDING));
        if (updated == 0) {
            LOG.warn("[Outbox] markPublishFailed affected 0 rows (concurrent update). outboxId={}",
                    outboxId);
        } else {
            LOG.info("[Outbox] Publish failed recorded. outboxId={}, retryCount incremented.", outboxId);
        }
    }

    /** Retry용 Outbox 생성. delivery가 PENDING/RETRY일 때만 생성. 이미 PENDING/SENDING Outbox 있으면 skip. */
    @Transactional
    public void createRetryOutboxIfAbsent(UUID deliveryId, UUID notificationId,
                                           NotificationChannel channel) {
        NotificationDelivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null || !RETRYABLE_DELIVERY_STATUSES.contains(delivery.getStatus())) {
            LOG.debug("[Outbox] Skip — delivery not found or not retryable. deliveryId={}, status={}",
                    deliveryId, delivery != null ? delivery.getStatus() : null);
            return;
        }

        boolean alreadyPending = outboxRepository.existsByDeliveryIdAndStatusIn(
                deliveryId,
                List.of(OutboxStatus.PENDING, OutboxStatus.SENDING));

        if (alreadyPending) {
            LOG.debug("[Outbox] Skip — pending outbox already exists. deliveryId={}", deliveryId);
            return;
        }

        NotificationOutbox outbox = NotificationOutbox.builder()
                .deliveryId(deliveryId)
                .notificationId(notificationId)
                .channel(channel)
                .build();
        outboxRepository.save(outbox);
        LOG.debug("[Outbox] Created retry outbox. deliveryId={}, channel={}", deliveryId, channel);
    }

    /** SENT/FAILED 중 retentionDays일 지난 레코드 삭제. */
    @Transactional
    public int cleanupCompletedOutbox(int retentionDays) {
        LocalDateTime before = LocalDateTime.now().minusDays(retentionDays);
        int deleted = outboxRepository.deleteCompletedOutboxBefore(
                List.of(OutboxStatus.SENT, OutboxStatus.FAILED),
                before);
        if (deleted > 0) {
            LOG.info("[Outbox] Cleaned up {} completed entries older than {} days.",
                    deleted, retentionDays);
        }
        return deleted;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
