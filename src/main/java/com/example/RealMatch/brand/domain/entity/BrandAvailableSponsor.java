package com.example.RealMatch.brand.domain.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.global.common.DeleteBaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brand_available_sponsor")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandAvailableSponsor extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(length = 100)
    private String name;

    @Column(length = 1000)
    private String content;

    // 1. 총 모집 인원 필드 추가
    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    // 2. 현재 모집된 인원 필드 추가 (기본값 0)
    @Column(name = "current_count", nullable = false)
    private Integer currentCount = 0;

    // 3. 이미지 리스트 (1:N 관계) 필드 추가
    // mappedBy는 BrandSponsorImage 엔티티에 있는 변수명과 일치해야 합니다.
    @OneToMany(mappedBy = "sponsor", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BrandSponsorImage> images = new ArrayList<>();

    @Builder
    public BrandAvailableSponsor(Campaign campaign, Brand brand, String name, String content, Integer totalCount) {
        this.campaign = campaign;
        this.brand = brand;
        this.name = name;
        this.content = content;
        this.totalCount = totalCount;
    }
}
