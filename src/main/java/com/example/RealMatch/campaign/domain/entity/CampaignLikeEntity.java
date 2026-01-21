package com.example.RealMatch.campaign.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.user.domain.entity.UserEntity;

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
@Table(name = "p_campaign_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignLikeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignEntity campaign;

    @Builder
    public CampaignLikeEntity(UserEntity user, CampaignEntity campaign) {
        this.user = user;
        this.campaign = campaign;
    }
}
