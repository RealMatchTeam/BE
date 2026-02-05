package com.example.RealMatch.brand.domain.entity;

import com.example.RealMatch.global.common.BaseEntity;

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
@Getter
@Table(name = "brand_describe_tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandDescribeTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;  

    @Column(name = "brand_describe_tag")
    private String brandDescribeTag;

    @Builder
    public BrandDescribeTag(Brand brand, String brandDescribeTag) {
        this.brand = brand;
        this.brandDescribeTag = brandDescribeTag;
    }
}
