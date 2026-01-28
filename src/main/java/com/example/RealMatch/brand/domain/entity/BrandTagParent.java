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
@Table(name = "brand_tag_parent")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandTagParent extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_parent_id")
    private Long tagParentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "tag_parent_name", nullable = false, length = 100)
    private String tagParentName;

    @Builder
    public BrandTagParent(Brand brand, String tagParentName) {
        this.brand = brand;
        this.tagParentName = tagParentName;
    }
}
