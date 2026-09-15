package com.example.RealMatch.notification.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.notification.application.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.application.repository.NotificationOutboxRepository;
import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.entity.enums.OutboxStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationOutboxService {
    private final NotificationOutboxRepository outboxes;
    private final NotificationDeliveryRepository deliveries;

    @Transactional(readOnly = true)
    public List<NotificationOutbox> findPendingOutbox(int limit) {
        return outboxes.findDue(LocalDateTime.now(), PageRequest.of(0, limit));
    }

    @Transactional
    public UUID claimOutbox(UUID id) {
        return outboxes.findForUpdate(id).map(o -> o.claim(LocalDateTime.now())).orElse(null);
    }

    @Transactional
    public void complete(UUID id, UUID token, String error) {
        outboxes.findForUpdate(id).ifPresent(o -> o.complete(token, error, LocalDateTime.now()));
    }

    @Transactional
    public void recover(UUID id, LocalDateTime before) {
        outboxes.findForUpdate(id).filter(o -> o.getStatus() == OutboxStatus.SENDING
                && o.getAttemptedAt() != null && o.getAttemptedAt().isBefore(before))
                .ifPresent(o -> o.complete(o.getClaimToken(), "Publisher lease expired", LocalDateTime.now()));
    }

    @Transactional
    public void createRetryOutboxIfAbsent(UUID id) {
        var delivery = deliveries.findForUpdate(id).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        if (delivery == null || delivery.getStatus() != DeliveryStatus.PENDING && delivery.getStatus() != DeliveryStatus.RETRY
                || delivery.getNextRetryAt() != null && delivery.getNextRetryAt().isAfter(now)) {
            return;
        }
        if (delivery.getLastEnqueuedAt() != null && !delivery.getLastEnqueuedAt().isBefore(now.minusMinutes(30))
                && !(delivery.getNextRetryAt() != null && delivery.getLastEnqueuedAt().isBefore(delivery.getAttemptedAt()))) {
            return;
        }
        var existing = outboxes.findByDeliveryId(id).orElse(null);
        if (existing != null) {
            var locked = outboxes.findForUpdate(existing.getId()).orElseThrow();
            if (locked.getStatus() == OutboxStatus.PENDING || locked.getStatus() == OutboxStatus.SENDING) {
                return;
            }
            if (locked.getStatus() == OutboxStatus.FAILED) {
                delivery.markAsPermanentlyFailed("Outbox publish attempts exhausted");
                return;
            }
            locked.requeue();
        } else {
            outboxes.save(NotificationOutbox.builder().deliveryId(id).notificationId(delivery.getNotificationId())
                    .channel(delivery.getChannel()).build());
        }
        delivery.enqueued(now);
    }

    @Transactional
    public int cleanupCompletedOutbox(int days) {
        var ids = outboxes.findCompleted(LocalDateTime.now().minusDays(days), PageRequest.of(0, 200));
        outboxes.deleteAllByIdInBatch(ids);
        return ids.size();
    }
}
