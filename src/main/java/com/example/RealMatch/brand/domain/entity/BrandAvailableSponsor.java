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
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(length = 100)
    private String name;

    @Column(length = 1000)
    private String content;

    @OneToMany(mappedBy = "sponsor", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BrandSponsorItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "sponsor", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BrandSponsorImage> images = new ArrayList<>();

    @Builder
    public BrandAvailableSponsor(
            Brand brand,
            String name,
            String content
    ) {
        this.brand = brand;
        this.name = name;
        this.content = content;
    }
}
