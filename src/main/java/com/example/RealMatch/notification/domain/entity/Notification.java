package com.example.RealMatch.notification.domain.entity;

import java.util.UUID;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.notification.domain.entity.enums.ReferenceType;

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
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_user_read_created", columnList = "user_id, is_read, created_at"),
        @Index(name = "idx_notification_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_notification_user_kind", columnList = "user_id, kind")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 30)
    private NotificationKind kind;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false, length = 1000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 30)
    private ReferenceType referenceType;

    @Column(name = "reference_id", length = 36)
    private String referenceId;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "proposal_id")
    private Long proposalId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Builder
    protected Notification(Long userId, NotificationKind kind, String title, String body,
                           ReferenceType referenceType, String referenceId,
                           Long campaignId, Long proposalId) {
        this.userId = userId;
        this.kind = kind;
        this.title = title;
        this.body = body;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.campaignId = campaignId;
        this.proposalId = proposalId;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
