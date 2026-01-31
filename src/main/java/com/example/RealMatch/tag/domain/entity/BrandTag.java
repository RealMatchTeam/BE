package com.example.RealMatch.tag.domain.entity;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "brand_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"brand_id", "tag_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Builder
    public BrandTag(
            Brand brand,
            Tag tag
    ) {
        this.brand = brand;
        this.tag = tag;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }
}
