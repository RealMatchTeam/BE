package com.example.RealMatch.business.domain.entity;

import java.util.UUID;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.tag.domain.entity.TagBeauty;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "campaign_proposal_beauty_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"campaign_proposal_id", "beauty_tag_id"})
        }
)
public class CampaignProposalBeautyTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_proposal_id", nullable = false)
    private CampaignProposal campaignProposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beauty_tag_id", nullable = false)
    private TagBeauty tagBeauty;

    @Builder
    public CampaignProposalBeautyTag(
            CampaignProposal campaignProposal,
            TagBeauty tagBeauty
    ) {
        this.campaignProposal = campaignProposal;
        this.tagBeauty = tagBeauty;
    }
}
