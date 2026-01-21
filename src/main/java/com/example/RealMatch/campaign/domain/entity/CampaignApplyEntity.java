package com.example.RealMatch.campaign.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.user.domain.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_campaign_apply")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignApplyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignEntity campaign;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Builder
    public CampaignApplyEntity(UserEntity user, CampaignEntity campaign, String reason) {
        this.user = user;
        this.campaign = campaign;
        this.reason = reason;
        this.isDeleted = false;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
