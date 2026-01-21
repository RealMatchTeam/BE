package com.example.RealMatch.brand.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.campaign.domain.entity.CampaignEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_brand_available_sponsor")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandAvailableSponsorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignEntity campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private BrandEntity brand;

    @Column(length = 100)
    private String name;

    @Column(length = 1000)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public BrandAvailableSponsorEntity(CampaignEntity campaign, BrandEntity brand, String name, String content) {
        this.campaign = campaign;
        this.brand = brand;
        this.name = name;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
