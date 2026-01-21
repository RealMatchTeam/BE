package com.example.RealMatch.brand.domain.entity;

import java.time.LocalDateTime;

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
@Table(name = "p_brand_sponsor_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandSponsorImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsor_id", nullable = false)
    private BrandAvailableSponsorEntity sponsor;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder
    public BrandSponsorImageEntity(BrandAvailableSponsorEntity sponsor, String imageUrl, Long createdBy) {
        this.sponsor = sponsor;
        this.imageUrl = imageUrl;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
    }

    public void softDelete(Long deletedBy) {
        this.isDeleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = LocalDateTime.now();
    }
}
