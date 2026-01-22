package com.example.RealMatch.brand.domain.entity;

import com.example.RealMatch.global.common.DeleteBaseEntity;

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
@Table(name = "brand_sponsor_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandSponsorImage extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsor_id", nullable = false)
    private BrandAvailableSponsor sponsor;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder
    public BrandSponsorImage(BrandAvailableSponsor sponsor, String imageUrl, Long createdBy) {
        this.sponsor = sponsor;
        this.imageUrl = imageUrl;
        this.createdBy = createdBy;
    }

    public void softDelete(Long deletedBy) {
        this.deletedBy = deletedBy;
    }
}
