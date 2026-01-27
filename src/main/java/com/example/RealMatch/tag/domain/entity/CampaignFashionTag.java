package com.example.RealMatch.tag.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
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
        name = "campaign_fashion_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"campaign_id", "fashion_tag_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignFashionTag extends BaseEntity {

    @Id
    @UuidGenerator
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fashion_tag_id", nullable = false)
    private TagFashion tagFashion;

    @Builder
    public CampaignFashionTag(
            Campaign campaign,
            TagFashion tagFashion
    ) {
        this.campaign = campaign;
        this.tagFashion = tagFashion;
    }
}
