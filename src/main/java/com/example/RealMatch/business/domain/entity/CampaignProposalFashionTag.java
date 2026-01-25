package com.example.RealMatch.business.domain.entity;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.tag.domain.entity.TagFashion;

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
        name = "campaign_proposal_fashion_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"campaign_proposal_id", "fashion_tag_id"})
        }
)
public class CampaignProposalFashionTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_proposal_id", nullable = false)
    private CampaignProposal campaignProposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fashion_tag_id", nullable = false)
    private TagFashion tagFashion;

    @Builder
    public CampaignProposalFashionTag(
            CampaignProposal campaignProposal,
            TagFashion tagFashion
    ) {
        this.campaignProposal = campaignProposal;
        this.tagFashion = tagFashion;
    }
}
