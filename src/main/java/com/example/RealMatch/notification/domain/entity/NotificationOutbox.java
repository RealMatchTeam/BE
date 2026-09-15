package com.example.RealMatch.notification.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.notification.domain.entity.enums.OutboxStatus;
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

/**
 * Outbox 패턴의 핵심 엔티티.
 *
 * <p>Notification + NotificationDelivery와 같은 트랜잭션에서 저장되며,
 * 트랜잭션 커밋 이후 별도 스케줄러(OutboxPublisher)가 이 레코드를 조회하여
 * RabbitMQ로 발행한다.
 *
 * <p>이 구조에 의해 "DB 저장은 성공했지만 MQ 발행이 누락"되는 시나리오를 방지한다.
 */
@Entity
@Table(name = "notification_outbox", indexes = {
    @Index(name = "idx_outbox_due", columnList = "status,next_attempt_at,created_at,id"),
    @Index(name = "idx_outbox_stuck", columnList = "status,attempted_at"),
    @Index(name = "idx_outbox_cleanup", columnList = "status,updated_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutbox extends BaseEntity {

    public static final int MAX_PUBLISH_RETRY = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "delivery_id", nullable = false, columnDefinition = "BINARY(16)", unique = true)
    private UUID deliveryId;

    @Column(name = "notification_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID notificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", length = 500)
    private String lastError;
    private LocalDateTime attemptedAt;
    private LocalDateTime nextAttemptAt;
    @Column(columnDefinition = "BINARY(16)")
    private UUID claimToken;

    public UUID claim(LocalDateTime now) {
        if (status != OutboxStatus.PENDING || nextAttemptAt != null && nextAttemptAt.isAfter(now)) {
            return null;
        }
        status = OutboxStatus.SENDING;
        attemptedAt = now;
        claimToken = UUID.randomUUID();
        retryCount++;
        return claimToken;
    }

    public void complete(UUID token, String error, LocalDateTime now) {
        if (status != OutboxStatus.SENDING || !Objects.equals(claimToken, token)) {
            return;
        }
        claimToken = null;
        if (error == null) {
            status = OutboxStatus.SENT;
            nextAttemptAt = null;
        } else {
            lastError = error.substring(0, Math.min(error.length(), 500));
            status = retryCount >= MAX_PUBLISH_RETRY ? OutboxStatus.FAILED : OutboxStatus.PENDING;
            nextAttemptAt = now.plusSeconds(Math.min(1L << Math.min(retryCount, 10), 900));
        }
    }

    public void requeue() {
        if (status == OutboxStatus.SENT || status == OutboxStatus.FAILED) {
            status = OutboxStatus.PENDING;
            retryCount = 0;
            nextAttemptAt = LocalDateTime.now();
            claimToken = null;
        }
    }

    @Builder
    protected NotificationOutbox(UUID deliveryId, UUID notificationId,
                                 NotificationChannel channel) {
        this.deliveryId = deliveryId;
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.nextAttemptAt = LocalDateTime.now();
    }
}
