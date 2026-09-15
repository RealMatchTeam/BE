package com.example.RealMatch.notification.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "notification_push_receipt", uniqueConstraints = @UniqueConstraint(columnNames = {"notification_id", "token_id"}),
        indexes = @Index(name = "idx_push_receipt_created", columnList = "created_at"))
public class PushReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "notification_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID notificationId;
    @Column(name = "token_id", nullable = false)
    private Long tokenId;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PushReceipt(UUID notificationId, Long tokenId) {
        this.notificationId = notificationId;
        this.tokenId = tokenId;
        this.createdAt = LocalDateTime.now();
    }
}
