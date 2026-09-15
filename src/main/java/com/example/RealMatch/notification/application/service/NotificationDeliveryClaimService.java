package com.example.RealMatch.notification.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.notification.application.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationDeliveryClaimService {
    private final NotificationDeliveryRepository repository;

    @Transactional
    public Claim claim(UUID id) {
        return repository.findForUpdate(id).filter(d -> d.claim(LocalDateTime.now()))
                .map(d -> new Claim(d.getId(), d.getNotificationId(), d.getChannel(), d.getAttemptCount()))
                .orElse(null);
    }

    @Transactional
    public void complete(Claim claim, DeliveryStatus result, String detail) {
        var delivery = repository.findForUpdate(claim.id()).orElse(null);
        if (delivery == null || delivery.getStatus() != DeliveryStatus.IN_PROGRESS
                || delivery.getAttemptCount() != claim.attempt()) {
            return;
        }
        switch (result) {
            case SENT -> delivery.markAsSent(detail);
            case RETRY -> delivery.recordFailure(detail);
            case FAILED -> delivery.markAsPermanentlyFailed(detail);
            case SKIPPED -> delivery.skip();
            default -> throw new IllegalArgumentException("Invalid delivery outcome");
        }
    }

    @Transactional
    public void recover(UUID id, LocalDateTime before) {
        repository.findForUpdate(id).filter(d -> d.getStatus() == DeliveryStatus.IN_PROGRESS
                && d.getAttemptedAt().isBefore(before)).ifPresent(d -> d.recordFailure("Worker lease expired"));
    }

    public record Claim(UUID id, UUID notificationId, NotificationChannel channel, int attempt) {
    }
}
