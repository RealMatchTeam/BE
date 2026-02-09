package com.example.RealMatch.notification.infrastructure.worker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.notification.application.service.NotificationDeliveryProcessor;
import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.repository.NotificationDeliveryRepository;

import lombok.RequiredArgsConstructor;

/**
 * PENDING 상태의 NotificationDelivery를 주기적으로 가져와 발송하는 스케줄러 워커.
 *
 * <p>설계 원칙:
 * <ul>
 *   <li>외부 I/O(FCM, Email)는 이 워커에서만 수행한다 (리스너에서 직접 호출 금지)</li>
 *   <li>각 delivery는 독립 트랜잭션으로 처리하여 장애를 격리한다</li>
 *   <li>재시도는 exponential backoff(1m, 5m, 30m, 2h, 12h) 기반</li>
 *   <li>워커 비정상 종료 시 IN_PROGRESS 상태 건은 별도 복구 로직으로 처리한다</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class NotificationDeliveryWorker {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDeliveryWorker.class);

    private static final int BATCH_SIZE = 50;

    private static final int STUCK_THRESHOLD_MINUTES = 10;

    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationDeliveryProcessor deliveryProcessor;

    @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
    public void processPendingDeliveries() {
        List<NotificationDelivery> pendingBatch = deliveryRepository.findRetryableDeliveries(
                DeliveryStatus.PENDING,
                LocalDateTime.now(),
                PageRequest.of(0, BATCH_SIZE));

        if (pendingBatch.isEmpty()) {
            return;
        }

        LOG.info("[DeliveryWorker] Processing {} pending deliveries.", pendingBatch.size());

        for (NotificationDelivery delivery : pendingBatch) {
            processWithFaultIsolation(delivery.getId());
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void recoverStuckDeliveries() {
        LocalDateTime stuckBefore = LocalDateTime.now().minusMinutes(STUCK_THRESHOLD_MINUTES);
        int recovered = deliveryRepository.recoverStuckDeliveries(
                DeliveryStatus.IN_PROGRESS, DeliveryStatus.PENDING, stuckBefore);
        if (recovered > 0) {
            LOG.warn("[DeliveryWorker] Recovered {} stuck deliveries.", recovered);
        }
    }

    private void processWithFaultIsolation(UUID deliveryId) {
        try {
            deliveryProcessor.processDelivery(deliveryId);
        } catch (Exception e) {
            // 절대 상위로 전파하지 않는다
            LOG.error("[DeliveryWorker] Unexpected error processing delivery. deliveryId={}",
                    deliveryId, e);
        }
    }
}
