package com.example.RealMatch.brand.domain.entity;

import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brand_sponsor_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandSponsorInfo extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsor_id", nullable = false, unique = true)
    private BrandAvailableSponsor sponsor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "shipping_type", length = 50)
    private String shippingType;

    @OneToMany(mappedBy = "sponsorInfo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BrandSponsorItem> items = new ArrayList<>();

    @Builder
    public BrandSponsorInfo(BrandAvailableSponsor sponsor, Brand brand, String shippingType) {
        this.sponsor = sponsor;
        this.brand = brand;
        this.shippingType = shippingType;
    }
}
