package com.example.RealMatch.notification.domain.entity;

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
    @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutbox extends BaseEntity {

    public static final int MAX_PUBLISH_RETRY = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "delivery_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID deliveryId;

    @Column(name = "notification_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID notificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Builder
    protected NotificationOutbox(UUID deliveryId, UUID notificationId,
                                 NotificationChannel channel) {
        this.deliveryId = deliveryId;
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
    }
}
