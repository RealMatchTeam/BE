package com.example.RealMatch.user.domain.entity;

import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_matching_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMatchingDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 뷰티 관련
    @Column(name = "skin_type")
    private String skinType;

    @Column(name = "skin_brightness")
    private String skinBrightness;

    @Column(name = "makeup_style")
    private String makeupStyle;

    @Column(name = "interest_categories")
    private String interestCategories;

    @Column(name = "interest_functions")
    private String interestFunctions;

    // 패션 관련
    @Column(name = "height")
    private String height;

    @Column(name = "weight")
    private String weight;

    @Column(name = "body_shape")
    private String bodyShape;

    @Column(name = "upper_size")
    private String topSize;

    @Column(name = "lower_size")
    private String bottomSize;

    @Column(name = "interest_fields")
    private String interestFields;

    @Column(name = "interest_styles")
    private String interestStyles;

    @Column(name = "interest_brands")
    private String interestBrands;

    // 콘텐츠 관련
    @Column(name = "sns_url")
    private String snsUrl;

    @Column(name = "viewer_gender")
    private String viewerGender;

    @Column(name = "viewer_age")
    private String viewerAge;

    @Column(name = "video_length")
    private String avgVideoLength;

    @Column(name = "views")
    private String avgViews;

    @Column(name = "content_formats")
    private String contentFormats;

    @Column(name = "content_tones")
    private String contentTones;

    @Column(name = "creator_type")
    private String creatorType;

    @Column(name = "good_with")
    private String goodWith;

    @Column(name = "desired_involvement")
    private String desiredInvolvement;

    @Column(name = "desired_usage_scope")
    private String desiredUsageScope;

    @Builder
    public UserMatchingDetail(
            Long userId,
            String height,
            String weight,
            String bodyShape,
            String topSize,
            String bottomSize,
            String interestFields,
            String interestStyles,
            String interestBrands,
            String skinType,
            String skinBrightness,
            String makeupStyle,
            String interestCategories,
            String interestFunctions,
            String snsUrl,
            String avgVideoLength,
            String avgViews,
            String viewerGender,
            String viewerAge,
            String contentFormats,
            String contentTones,
            String creatorType,
            String goodWith,
            String desiredInvolvement,
            String desiredUsageScope
    ) {
        this.userId = userId;
        this.height = height;
        this.weight = weight;
        this.bodyShape = bodyShape;
        this.topSize = topSize;
        this.bottomSize = bottomSize;
        this.interestFields = interestFields;
        this.interestStyles = interestStyles;
        this.interestBrands = interestBrands;
        this.skinType = skinType;
        this.skinBrightness = skinBrightness;
        this.makeupStyle = makeupStyle;
        this.interestCategories = interestCategories;
        this.interestFunctions = interestFunctions;
        this.snsUrl = snsUrl;
        this.avgVideoLength = avgVideoLength;
        this.avgViews = avgViews;
        this.viewerGender = viewerGender;
        this.viewerAge = viewerAge;
        this.contentFormats = contentFormats;
        this.contentTones = contentTones;
        this.creatorType = creatorType;
        this.goodWith = goodWith;
        this.desiredInvolvement = desiredInvolvement;
        this.desiredUsageScope = desiredUsageScope;
    }

    // ========== 비즈니스 메서드 (도메인 로직) ==========

    /**
     * 뷰티 특성 업데이트
     */
    public void updateBeautyFeatures(
            String skinType,
            String skinBrightness,
            String makeupStyle,
            String interestCategories,
            String interestFunctions
    ) {
        if (skinType != null) {
            this.skinType = skinType;
        }
        if (skinBrightness != null) {
            this.skinBrightness = skinBrightness;
        }
        if (makeupStyle != null) {
            this.makeupStyle = makeupStyle;
        }
        if (interestCategories != null) {
            this.interestCategories = interestCategories;
        }
        if (interestFunctions != null) {
            this.interestFunctions = interestFunctions;
        }
    }

    /**
     * 패션 특성 업데이트
     */
    public void updateFashionFeatures(
            String height,
            String weight,
            String bodyShape,
            String topSize,
            String bottomSize,
            String interestFields,
            String interestStyles,
            String interestBrands
    ) {
        if (height != null) {
            this.height = height;
        }
        if (weight != null) {
            this.weight = weight;
        }
        if (bodyShape != null) {
            this.bodyShape = bodyShape;
        }
        if (topSize != null) {
            this.topSize = topSize;
        }
        if (bottomSize != null) {
            this.bottomSize = bottomSize;
        }
        if (interestFields != null) {
            this.interestFields = interestFields;
        }
        if (interestStyles != null) {
            this.interestStyles = interestStyles;
        }
        if (interestBrands != null) {
            this.interestBrands = interestBrands;
        }
    }

    /**
     * 콘텐츠 특성 업데이트
     */
    public void updateContentsFeatures(
            String viewerGender,
            String viewerAge,
            String avgVideoLength,
            String avgViews,
            String contentFormats,
            String contentTones,
            String desiredInvolvement,
            String desiredUsageScope
    ) {
        if (viewerGender != null) {
            this.viewerGender = viewerGender;
        }
        if (viewerAge != null) {
            this.viewerAge = viewerAge;
        }
        if (avgVideoLength != null) {
            this.avgVideoLength = avgVideoLength;
        }
        if (avgViews != null) {
            this.avgViews = avgViews;
        }
        if (contentFormats != null) {
            this.contentFormats = contentFormats;
        }
        if (contentTones != null) {
            this.contentTones = contentTones;
        }
        if (desiredInvolvement != null) {
            this.desiredInvolvement = desiredInvolvement;
        }
        if (desiredUsageScope != null) {
            this.desiredUsageScope = desiredUsageScope;
        }
    }
}
