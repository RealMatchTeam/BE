package com.example.RealMatch.brand.domain.entity;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.tag.domain.entity.TagContent;

import jakarta.persistence.Column;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "brand_content_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"brand_id", "content_tag_id"})
        }
)
public class BrandContentTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_tag_id", nullable = false)
    private TagContent tagContent;

    @Column(name = "custom_tag_value")
    private String customTagValue;

    private BrandContentTag(
            Brand brand,
            TagContent tagContent,
            String customTagValue
    ) {
        this.brand = brand;
        this.tagContent = tagContent;

        if (tagContent.getEngName().equals("ETC")) {
            if (customTagValue == null || customTagValue.isBlank()) {
                throw new IllegalArgumentException("ETC 태그에는 입력값이 필요합니다.");
            }
            this.customTagValue = customTagValue;
        } else {
            this.customTagValue = null;
        }
    }

    public static BrandContentTag of(Brand brand, TagContent tagContent) {
        return new BrandContentTag(brand, tagContent, null);
    }

    public static BrandContentTag ofCustom(Brand brand, TagContent tagContent, String customValue) {
        return new BrandContentTag(brand, tagContent, customValue);
    }
}
