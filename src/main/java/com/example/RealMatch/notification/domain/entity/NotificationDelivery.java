package com.example.RealMatch.notification.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_delivery", indexes = {
        @Index(name = "idx_delivery_notification", columnList = "notification_id, channel"),
        @Index(name = "idx_delivery_status", columnList = "status, attempted_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationDelivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "notification_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID notificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "fail_reason", length = 500)
    private String failReason;

    @Column(name = "attempted_at")
    private LocalDateTime attemptedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Builder
    protected NotificationDelivery(UUID notificationId, NotificationChannel channel,
                                   DeliveryStatus status, String idempotencyKey) {
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.attemptedAt = LocalDateTime.now();
        this.attemptCount = 0;
    }

    public void markAsSent(String providerMessageId) {
        this.status = DeliveryStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.providerMessageId = providerMessageId;
    }

    public void markAsFailed(String failReason) {
        this.status = DeliveryStatus.FAILED;
        this.failReason = failReason;
        this.attemptCount++;
    }

    public void markAsInProgress() {
        this.status = DeliveryStatus.IN_PROGRESS;
        this.attemptCount++;
        this.attemptedAt = LocalDateTime.now();
    }
}
