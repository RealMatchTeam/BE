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
        @Index(name = "idx_delivery_status_retry", columnList = "status, next_retry_at, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationDelivery extends BaseEntity {

    public static final int MAX_RETRY_COUNT = 5;
    private static final long[] BACKOFF_MINUTES = {1, 5, 30, 120, 720};

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

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

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

    /** 발송 성공 */
    public void markAsSent(String providerMessageId) {
        this.status = DeliveryStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.providerMessageId = providerMessageId;
        this.nextRetryAt = null;
    }

    /** 발송 진행 중 (워커가 점유) */
    public void markAsInProgress() {
        this.status = DeliveryStatus.IN_PROGRESS;
        this.attemptedAt = LocalDateTime.now();
    }

    /**
     * 발송 실패 기록 + 재시도 스케줄링.
     * attemptCount < MAX_RETRY_COUNT → PENDING + nextRetryAt 설정
     * attemptCount ≥ MAX_RETRY_COUNT → FAILED (영구 보관)
     */
    public void recordFailure(String failReason) {
        this.failReason = truncate(failReason, 500);
        this.attemptCount++;

        if (this.attemptCount >= MAX_RETRY_COUNT) {
            this.status = DeliveryStatus.FAILED;
            this.nextRetryAt = null;
        } else {
            this.status = DeliveryStatus.PENDING;
            int backoffIndex = Math.min(this.attemptCount - 1, BACKOFF_MINUTES.length - 1);
            this.nextRetryAt = LocalDateTime.now().plusMinutes(BACKOFF_MINUTES[backoffIndex]);
        }
    }

    /** 영구 실패 처리 */
    public void markAsPermanentlyFailed(String failReason) {
        this.status = DeliveryStatus.FAILED;
        this.failReason = truncate(failReason, 500);
        this.nextRetryAt = null;
    }

    public boolean isRetryable() {
        return this.attemptCount < MAX_RETRY_COUNT;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
