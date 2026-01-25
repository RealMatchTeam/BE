package com.example.RealMatch.business.domain.entity;

import java.util.UUID;

import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.tag.domain.entity.TagContent;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "campaign_content_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"campaign_id", "content_tag_id"})
        }
)
public class CampaignContentTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_tag_id", nullable = false)
    private TagContent tagContent;

    @Builder
    public CampaignContentTag(
            Campaign campaign,
            TagContent tagContent
    ) {
        this.campaign = campaign;
        this.tagContent = tagContent;
    }

}
