package com.example.RealMatch.notification.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.domain.entity.enums.OutboxStatus;
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

    private final NotificationOutboxRepository outboxRepository;

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

    /** 발행 실패 시 retryCount &lt; MAX → PENDING, else FAILED. */
    @Transactional
    public void markOutboxPublishFailed(UUID outboxId, String error) {
        NotificationOutbox outbox = outboxRepository.findById(outboxId).orElse(null);
        if (outbox == null) {
            LOG.warn("[Outbox] Not found for failure recording. outboxId={}", outboxId);
            return;
        }

        OutboxStatus newStatus;
        if (outbox.getRetryCount() + 1 >= NotificationOutbox.MAX_PUBLISH_RETRY) {
            newStatus = OutboxStatus.FAILED;
            LOG.error("[Outbox] Max publish retries exceeded. outboxId={}, deliveryId={}",
                    outboxId, outbox.getDeliveryId());
        } else {
            newStatus = OutboxStatus.PENDING;
            LOG.warn("[Outbox] Publish failed, will retry. outboxId={}, retryCount={}",
                    outboxId, outbox.getRetryCount() + 1);
        }

        int updated = outboxRepository.markPublishFailed(
                outboxId,
                newStatus,
                truncate(error, 500),
                List.of(OutboxStatus.SENDING, OutboxStatus.PENDING));
        if (updated == 0) {
            LOG.warn("[Outbox] markPublishFailed affected 0 rows (concurrent update). outboxId={}",
                    outboxId);
        }
    }

    /** Retry용 Outbox 생성. 이미 PENDING/SENDING 있으면 skip(중복 방지). */
    @Transactional
    public void createRetryOutboxIfAbsent(UUID deliveryId, UUID notificationId,
                                           NotificationChannel channel) {
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
