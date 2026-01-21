package com.example.RealMatch.business.domain;

import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "campaign_content_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"campaign_id", "content_tag_id"})
        }
)
public class CampaignContentTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_tag_id", nullable = false)
    private ContentTag contentTag;

    @Column(name = "custom_tag_value")
    private String customTagValue;

    protected CampaignContentTag() {}

    private CampaignContentTag(
            Campaign campaign,
            ContentTag contentTag,
            String customTagValue
    ) {
        this.campaign = campaign;
        this.contentTag = contentTag;

        if (contentTag.getEngName().equals("ETC")) {
            if (customTagValue == null || customTagValue.isBlank()) {
                throw new IllegalArgumentException("ETC 태그에는 입력값이 필요합니다.");
            }
            this.customTagValue = customTagValue;
        } else {
            this.customTagValue = null;
        }
    }

    /* 일반 태그 */
    public static CampaignContentTag of(
            Campaign campaign,
            ContentTag contentTag
    ) {
        return new CampaignContentTag(campaign, contentTag, null);
    }

    /* 사용자 입력 태그 */
    public static CampaignContentTag ofCustom(
            Campaign campaign,
            ContentTag contentTag,
            String customValue
    ) {
        return new CampaignContentTag(campaign, contentTag, customValue);
    }
}
