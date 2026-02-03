package com.example.RealMatch.business.domain.entity;

import com.example.RealMatch.business.domain.enums.ProposalTagType;
import com.example.RealMatch.global.common.BaseEntity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "campaign_proposal_content_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"campaign_proposal_id", "tag_type", "tag_name"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignProposalContentTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_proposal_id", nullable = false)
    private CampaignProposal campaignProposal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type", nullable = false, length = 20)
    private ProposalTagType tagType;

    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName;

    @Column(name = "custom_tag_value", length = 255)
    private String customTagValue;

    @Builder
    public CampaignProposalContentTag(
            CampaignProposal campaignProposal,
            ProposalTagType tagType,
            String tagName,
            String customTagValue
    ) {
        this.campaignProposal = campaignProposal;
        this.tagType = tagType;
        this.tagName = tagName;
        this.customTagValue = normalize(customTagValue);
    }

    public static CampaignProposalContentTag create(
            CampaignProposal campaignProposal,
            ProposalTagType tagType,
            String tagName,
            String customTagValue
    ) {
        if (campaignProposal == null) {
            throw new IllegalArgumentException("campaignProposal은 null일 수 없습니다.");
        }
        if (tagType == null) {
            throw new IllegalArgumentException("tagType은 null일 수 없습니다.");
        }
        if (tagName == null || tagName.isBlank()) {
            throw new IllegalArgumentException("tagName은 null이거나 비어있을 수 없습니다.");
        }

        return CampaignProposalContentTag.builder()
                .campaignProposal(campaignProposal)
                .tagType(tagType)
                .tagName(tagName.trim())
                .customTagValue(customTagValue)
                .build();
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
