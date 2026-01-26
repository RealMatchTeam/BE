package com.example.RealMatch.tag.domain.entity;

import java.util.UUID;

import com.example.RealMatch.campaign.domain.entity.Campaign;
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
@Entity
@Table(
        name = "campaign_beauty_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"campaign_id", "beauty_tag_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignBeautyTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beauty_tag_id", nullable = false)
    private TagBeauty tagBeauty;

    @Builder
    public CampaignBeautyTag(
            Campaign campaign,
            TagBeauty tagBeauty
    ) {
        this.campaign = campaign;
        this.tagBeauty = tagBeauty;
    }
}
