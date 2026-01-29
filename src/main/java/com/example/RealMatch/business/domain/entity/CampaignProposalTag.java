package com.example.RealMatch.business.domain.entity;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.tag.domain.entity.Tag;

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
        name = "campaign_proposal_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"campaign_proposal_id", "tag_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignProposalTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_proposal_id", nullable = false)
    private CampaignProposal campaignProposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(name = "custom_tag_value")
    private String customTagValue;

    @Builder
    public CampaignProposalTag(
            CampaignProposal campaignProposal,
            Tag tag,
            String customTagValue
    ) {
        this.campaignProposal = campaignProposal;
        this.tag = tag;
        this.customTagValue = normalize(customTagValue);
    }

    public static CampaignProposalTag create(
            CampaignProposal campaignProposal,
            Tag tag,
            String customTagValue
    ) {
        if (campaignProposal == null) {
            throw new IllegalArgumentException("campaignProposal은 null일 수 없습니다.");
        }
        if (tag == null) {
            throw new IllegalArgumentException("tag는 null일 수 없습니다.");
        }

        return CampaignProposalTag.builder()
                .campaignProposal(campaignProposal)
                .tag(tag)
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
