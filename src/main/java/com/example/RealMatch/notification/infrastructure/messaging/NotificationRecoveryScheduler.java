package com.example.RealMatch.notification.infrastructure.messaging;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.RealMatch.notification.application.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.application.repository.NotificationOutboxRepository;
import com.example.RealMatch.notification.application.repository.PushReceiptRepository;
import com.example.RealMatch.notification.application.service.NotificationDeliveryClaimService;
import com.example.RealMatch.notification.application.service.NotificationOutboxService;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.entity.enums.OutboxStatus;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRecoveryScheduler {
    private final NotificationDeliveryRepository deliveries;
    private final NotificationOutboxRepository outboxes;
    private final NotificationOutboxService outboxService;
    private final NotificationDeliveryClaimService claims;
    private final MeterRegistry metrics;
    private final PushReceiptRepository receipts;
    private final Map<String, AtomicLong> gauges = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000, scheduler = "notificationRecoveryExecutor")
    public void recover() {
        var now = LocalDateTime.now();
        var page = PageRequest.of(0, 100);
        for (var id : deliveries.findStuck(now.minusMinutes(10), page)) {
            safely(() -> claims.recover(id, now.minusMinutes(10)));
        }
        for (var id : outboxes.findStuck(now.minusMinutes(5), page)) {
            safely(() -> outboxService.recover(id, now.minusMinutes(5)));
        }
        for (var delivery : deliveries.findDispatchable(now, now.minusMinutes(30), page)) {
            safely(() -> outboxService.createRetryOutboxIfAbsent(delivery.getId()));
        }
        age("notification.delivery.oldest.seconds", deliveries.oldestPending(), now);
        age("notification.outbox.oldest.seconds", outboxes.oldestPending(), now);
        for (var status : DeliveryStatus.values()) {
            gauge("notification.delivery.backlog", status.name(), deliveries.countByStatus(status));
        }
        for (var status : OutboxStatus.values()) {
            gauge("notification.outbox.backlog", status.name(), outboxes.countByStatus(status));
        }
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 120_000, scheduler = "notificationRecoveryExecutor")
    public void cleanupCompletedOutbox() {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        // Bounded transactions and a time budget keep recovery responsive during a cleanup backlog.
        for (int batch = 0; batch < 20 && System.nanoTime() < deadline; batch++) {
            int deleted = outboxService.cleanupCompletedOutbox(7);
            var ids = receipts.findCompleted(LocalDateTime.now().minusDays(30), PageRequest.of(0, 200));
            receipts.deleteAllByIdInBatch(ids);
            if (deleted < 200 && ids.size() < 200) {
                break;
            }
        }
    }

    private void age(String name, LocalDateTime oldest, LocalDateTime now) {
        gauge(name, "pending", oldest == null ? 0 : Math.max(0, Duration.between(oldest, now).getSeconds()));
    }

    private void gauge(String name, String status, long count) {
        gauges.computeIfAbsent(name + status, key -> metrics.gauge(name,
                List.of(Tag.of("status", status)),
                new AtomicLong())).set(count);
    }

    private void safely(Runnable work) {
        try {
            work.run();
        } catch (RuntimeException ex) {
            metrics.counter("notification.recovery.failures").increment();
            log.error("Notification recovery failed", ex);
        }
    }
}
