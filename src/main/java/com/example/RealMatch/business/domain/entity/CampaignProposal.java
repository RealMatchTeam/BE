package com.example.RealMatch.business.domain.entity;

import java.util.UUID;

import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.business.domain.enums.WhoProposed;
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
@Table(name = "campaign_proposal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignProposal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "who_proposed", nullable = false, length = 20)
    private WhoProposed whoProposed;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "refusal_reason", length = 1000)
    private String refusalReason;

    @Column(name = "proposal_description", nullable = false, length = 2000)
    private String proposalDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProposalStatus status;

    @Builder
    public CampaignProposal(Campaign campaign, User user, WhoProposed whoProposed,
                            String reason, String proposalDescription) {
        this.campaign = campaign;
        this.user = user;
        this.whoProposed = whoProposed;
        this.reason = reason;
        this.proposalDescription = proposalDescription;
        this.status = ProposalStatus.NONE;
    }

    public void updateStatus(ProposalStatus status) {
        this.status = status;
    }

    public void reject(String refusalReason) {
        this.status = ProposalStatus.REJECTED;
        this.refusalReason = refusalReason;
    }

    public void match() {
        this.status = ProposalStatus.MATCHED;
    }
}
