package com.example.RealMatch.business.domain;

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
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "campaign_proposal_content_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"campaign_proposal_id", "content_tag_id"})
        }
)
public class CampaignProposalContentTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_proposal_id", nullable = false)
    private CampaignProposal campaignProposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_tag_id", nullable = false)
    private TagContent tagContent;

    @Column(name = "custom_tag_value")
    private String customTagValue;

    protected CampaignProposalContentTag() {}

    private CampaignProposalContentTag(
            CampaignProposal campaignProposal,
            TagContent tagContent,
            String customTagValue
    ) {
        this.campaignProposal = campaignProposal;
        this.tagContent = tagContent;

        if (tagContent.getEngName().equals("ETC")) {
            if (customTagValue == null || customTagValue.isBlank()) {
                throw new IllegalArgumentException("ETC 태그에는 입력값이 필요합니다.");
            }
            this.customTagValue = customTagValue;
        } else {
            this.customTagValue = null;
        }
    }

    /* 일반 태그 */
    public static CampaignProposalContentTag of(
            CampaignProposal campaignProposal,
            TagContent tagContent
    ) {
        return new CampaignProposalContentTag(campaignProposal, tagContent, null);
    }

    /* 사용자 입력 태그 */
    public static CampaignProposalContentTag ofCustom(
            CampaignProposal campaignProposal,
            TagContent tagContent,
            String customTagValue
    ) {
        return new CampaignProposalContentTag(campaignProposal, tagContent, customTagValue);
    }
}
