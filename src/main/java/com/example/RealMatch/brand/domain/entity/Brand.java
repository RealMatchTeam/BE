package com.example.RealMatch.brand.domain.entity;

import com.example.RealMatch.brand.domain.entity.enums.IndustryType;
import com.example.RealMatch.global.common.DeleteBaseEntity;
import com.example.RealMatch.user.domain.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brand")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_name", nullable = false, length = 255)
    private String brandName;

    @Enumerated(EnumType.STRING)
    @Column(name = "industry_type", nullable = false, length = 20)
    private IndustryType industryType;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "simple_intro", length = 200)
    private String simpleIntro;

    @Column(name = "detail_intro", columnDefinition = "TEXT")
    private String detailIntro;

    @Column(name = "homepage_url", length = 500)
    private String homepageUrl;

    @Column(name = "matching_rate")
    private Integer matchingRate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true   // ⭐ 1:1 보장
    )
    private User user;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder
    public Brand(String brandName, IndustryType industryType, String logoUrl,
                 String simpleIntro, String detailIntro, String homepageUrl,
                 Integer matchingRate, Long createdBy) {
        this.brandName = brandName;
        this.industryType = industryType;
        this.logoUrl = logoUrl;
        this.simpleIntro = simpleIntro;
        this.detailIntro = detailIntro;
        this.homepageUrl = homepageUrl;
        this.matchingRate = matchingRate;
        this.createdBy = createdBy;
        this.user = user;
    }

    public void update(String brandName, String logoUrl, String simpleIntro,
                       String detailIntro, String homepageUrl, Long updatedBy) {
        this.brandName = brandName;
        this.logoUrl = logoUrl;
        this.simpleIntro = simpleIntro;
        this.detailIntro = detailIntro;
        this.homepageUrl = homepageUrl;
        this.updatedBy = updatedBy;
    }

    public void softDelete(Long deletedBy) {
        this.deletedBy = deletedBy;
    }
}
