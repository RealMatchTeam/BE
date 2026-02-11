package com.example.RealMatch.notification.infrastructure.messaging;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.notification.application.service.NotificationOutboxService;
import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.entity.enums.OutboxStatus;
import com.example.RealMatch.notification.domain.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.domain.repository.NotificationOutboxRepository;

import lombok.RequiredArgsConstructor;

/**
 * 장애 복구 스케줄러.
 *
 * <p>복구 대상:
 * 1. Stuck IN_PROGRESS Delivery → RETRY (10분 임계)
 * 2. Due RETRY Delivery → 새 Outbox 생성 (중복 방지)
 * 3. Stuck SENDING Outbox → PENDING (5분 임계)
 * 4. Orphaned PENDING Delivery → 새 Outbox 생성 (활성 Outbox 없는 고아)
 * 5. Completed Outbox Cleanup (7일 보관)
 */
@Component
@RequiredArgsConstructor
public class NotificationRecoveryScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRecoveryScheduler.class);

    private static final int DELIVERY_STUCK_THRESHOLD_MINUTES = 10;
    private static final int OUTBOX_STUCK_THRESHOLD_MINUTES = 5;
    private static final int ORPHAN_THRESHOLD_MINUTES = 30;
    private static final int RETRY_BATCH_SIZE = 50;
    private static final int OUTBOX_RETENTION_DAYS = 7;

    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationOutboxRepository outboxRepository;
    private final NotificationOutboxService outboxService;

    /** Consumer crash 복구: 10분 이상 IN_PROGRESS → RETRY */
    @Transactional
    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void recoverStuckDeliveries() {
        LocalDateTime stuckBefore = LocalDateTime.now()
                .minusMinutes(DELIVERY_STUCK_THRESHOLD_MINUTES);

        int recovered = deliveryRepository.recoverStuckDeliveries(
                DeliveryStatus.IN_PROGRESS,
                DeliveryStatus.RETRY,
                stuckBefore);

        if (recovered > 0) {
            LOG.warn("[Recovery] Recovered {} stuck IN_PROGRESS deliveries → RETRY.", recovered);
        }
    }

    /** backoff 만료된 RETRY delivery → 새 Outbox 생성하여 MQ 재발행 */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void reprocessRetryDeliveries() {
        List<NotificationDelivery> retryList = deliveryRepository.findRetryableDeliveries(
                DeliveryStatus.RETRY,
                LocalDateTime.now(),
                PageRequest.of(0, RETRY_BATCH_SIZE));

        if (retryList.isEmpty()) {
            return;
        }

        LOG.info("[Recovery] Found {} RETRY deliveries due for reprocessing.", retryList.size());

        for (NotificationDelivery delivery : retryList) {
            try {
                outboxService.createRetryOutboxIfAbsent(
                        delivery.getId(),
                        delivery.getNotificationId(),
                        delivery.getChannel());
            } catch (Exception e) {
                LOG.error("[Recovery] Failed to create retry outbox. deliveryId={}",
                        delivery.getId(), e);
            }
        }
    }

    /** Publisher crash 복구: 5분 이상 SENDING → PENDING */
    @Transactional
    @Scheduled(fixedDelay = 300_000, initialDelay = 90_000)
    public void recoverStuckOutbox() {
        LocalDateTime stuckBefore = LocalDateTime.now()
                .minusMinutes(OUTBOX_STUCK_THRESHOLD_MINUTES);

        int recovered = outboxRepository.recoverStuckOutbox(
                OutboxStatus.SENDING,
                OutboxStatus.PENDING,
                stuckBefore);

        if (recovered > 0) {
            LOG.warn("[Recovery] Recovered {} stuck SENDING outbox → PENDING.", recovered);
        }
    }

    /** Outbox FAILED 후 PENDING으로 남은 고아 delivery 복구 (30분 threshold) */
    @Scheduled(fixedDelay = 600_000, initialDelay = 180_000)
    public void recoverOrphanedPendingDeliveries() {
        LocalDateTime orphanBefore = LocalDateTime.now()
                .minusMinutes(ORPHAN_THRESHOLD_MINUTES);

        List<NotificationDelivery> orphanList = deliveryRepository.findOrphanedPendingDeliveries(
                DeliveryStatus.PENDING,
                orphanBefore,
                PageRequest.of(0, RETRY_BATCH_SIZE));

        if (orphanList.isEmpty()) {
            return;
        }

        LOG.warn("[Recovery] Found {} orphaned PENDING deliveries (no active outbox).",
                orphanList.size());

        for (NotificationDelivery delivery : orphanList) {
            try {
                outboxService.createRetryOutboxIfAbsent(
                        delivery.getId(),
                        delivery.getNotificationId(),
                        delivery.getChannel());
            } catch (Exception e) {
                LOG.error("[Recovery] Failed to create outbox for orphaned delivery. deliveryId={}",
                        delivery.getId(), e);
            }
        }
    }

    /** 7일 지난 SENT/FAILED Outbox 삭제 — 테이블 무한 성장 방지 */
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 120_000)
    public void cleanupCompletedOutbox() {
        outboxService.cleanupCompletedOutbox(OUTBOX_RETENTION_DAYS);
    }
}
