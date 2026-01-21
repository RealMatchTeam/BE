package com.example.RealMatch.business.domain;

import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.user.domain.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "campaign_apply")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignApply extends BaseEntity {

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
    @Enumerated(EnumType.STRING)
    private ProposalStatus proposalStatus;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Builder
    public CampaignApply(User user, Campaign campaign, ProposalStatus proposalStatus, String reason) {
        this.user = user;
        this.campaign = campaign;
        this.reason = reason;
        this.proposalStatus = ProposalStatus.REVIEWING;
    }

}
