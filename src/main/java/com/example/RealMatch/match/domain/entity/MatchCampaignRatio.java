package com.example.RealMatch.match.domain.entity;

import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.global.common.DeleteBaseEntity;
import com.example.RealMatch.user.domain.entity.User;

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
@Table(name = "match_campaign_ratio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchCampaignRatio extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false)
    private Long ratio;

    @Builder
    public MatchCampaignRatio(User user, Campaign campaign, Long ratio) {
        this.user = user;
        this.campaign = campaign;
        this.ratio = ratio;
    }

    public void updateRatio(Long ratio) {
        this.ratio = ratio;
    }

}
