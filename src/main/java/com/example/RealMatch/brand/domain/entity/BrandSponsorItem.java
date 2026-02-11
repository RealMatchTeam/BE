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
@Table(name = "brand_sponsor_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandSponsorItem extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsor_id", nullable = false)
    private BrandAvailableSponsor sponsor;

    @Column(name = "available_type", length = 30)
    private String availableType;

    @Column(name = "available_quantity")
    private Integer availableQuantity;

    @Column(name = "available_size")
    private Integer availableSize;

    @Column(name = "shipping_type", length = 30)
    private String shippingType;

    @Builder
    public BrandSponsorItem(
            BrandAvailableSponsor sponsor,
            String availableType,
            Integer availableQuantity,
            Integer availableSize,
            String shippingType
    ) {
        this.sponsor = sponsor;
        this.availableType = availableType;
        this.availableQuantity = availableQuantity;
        this.availableSize = availableSize;
        this.shippingType = shippingType;
    }
}
