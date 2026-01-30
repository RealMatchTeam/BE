package com.example.RealMatch.business.domain.entity;

import java.util.UUID;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.tag.domain.entity.TagContent;

import jakarta.persistence.Column;
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
@Entity
@Table(
        name = "campaign_proposal_content_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"campaign_proposal_id", "content_tag_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignProposalContentTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_proposal_id", nullable = false)
    private CampaignProposal campaignProposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_tag_id", nullable = false)
    private TagContent tagContent;

    @Column(name = "custom_tag_value")
    private String customTagValue;

    @Builder
    public CampaignProposalContentTag(
            CampaignProposal campaignProposal,
            TagContent tagContent,
            String customTagValue
    ) {
        this.campaignProposal = campaignProposal;
        this.tagContent = tagContent;
        this.customTagValue = customTagValue;
    }

    public static CampaignProposalContentTag create(
            CampaignProposal campaignProposal,
            TagContent tagContent,
            String customTagValue
    ) {
        if (campaignProposal == null) {
            throw new IllegalArgumentException("campaignProposal은 null일 수 없습니다.");
        }
        if (tagContent == null) {
            throw new IllegalArgumentException("tagContent는 null일 수 없습니다.");
        }

        return CampaignProposalContentTag.builder()
                .campaignProposal(campaignProposal)
                .tagContent(tagContent)
                .customTagValue(normalize(customTagValue))
                .build();
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
