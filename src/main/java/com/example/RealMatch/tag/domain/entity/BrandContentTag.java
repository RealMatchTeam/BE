package com.example.RealMatch.tag.domain.entity;

import java.util.UUID;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.tag.domain.entity.TagContent;

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
        name = "brand_content_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"brand_id", "content_tag_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandContentTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_tag_id", nullable = false)
    private TagContent tagContent;

    @Builder
    public BrandContentTag(
            Brand brand,
            TagContent tagContent
    ) {
        this.brand = brand;
        this.tagContent = tagContent;
    }
}
