package com.example.RealMatch.match.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.campaign.domain.entity.CampaignEntity;
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
@Table(name = "p_match_campaign_ratio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchCampaignRatioEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignEntity campaign;

    @Column(nullable = false)
    private Long ratio;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public MatchCampaignRatioEntity(UserEntity user, CampaignEntity campaign, Long ratio) {
        this.user = user;
        this.campaign = campaign;
        this.ratio = ratio;
        this.isDeleted = false;
    }

    public void updateRatio(Long ratio) {
        this.ratio = ratio;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
