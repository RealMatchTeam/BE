package com.example.RealMatch.user.domain.entity;

import java.util.List;
import java.util.stream.Collectors;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;

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

    // 매칭 결과 (검사 완료 후 설정됨)
    @Column(name = "creator_type")
    private String creatorType;

    @Column(name = "desired_involvement")
    private String desiredInvolvement;

    @Column(name = "desired_usage_scope")
    private String desiredUsageScope;

    @Column(name = "is_deprecated", nullable = false)
    private Boolean isDeprecated = false;

    @Builder
    public UserMatchingDetail(
            Long userId,
            String height,
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
            String desiredInvolvement,
            String desiredUsageScope
    ) {
        this.userId = userId;
        this.height = height;
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
        this.desiredInvolvement = desiredInvolvement;
        this.desiredUsageScope = desiredUsageScope;
        this.isDeprecated = false;
    }

    // =======================
    // 정적 팩토리 (DTO -> Entity)
    // =======================
    public static UserMatchingDetail from(Long userId, MatchRequestDto requestDto) {

        if (requestDto == null) {
            return UserMatchingDetail.builder()
                    .userId(userId)
                    .build();
        }

        // Fashion
        String height = null;
        String bodyShape = null;
        String topSize = null;
        String bottomSize = null;
        String interestFields = null;
        String interestStyles = null;
        String interestBrands = null;

        if (requestDto.getFashion() != null) {
            var f = requestDto.getFashion();
            height = toCsv(f.getHeightTag());
            bodyShape = toCsv(f.getWeightTypeTag());
            topSize = toCsv(f.getTopSizeTag());
            bottomSize = toCsv(f.getBottomSizeTag());
            interestFields = toCsvList(f.getPreferredItemTags());
            interestStyles = toCsvList(f.getInterestStyleTags());
            interestBrands = toCsvList(f.getPreferredBrandTags());
        }

        // Beauty
        String skinType = null;
        String skinBrightness = null;
        String makeupStyle = null;
        String interestCategories = null;
        String interestFunctions = null;

        if (requestDto.getBeauty() != null) {
            var b = requestDto.getBeauty();
            skinType = toCsv(b.getSkinTypeTags());
            skinBrightness = toCsv(b.getSkinToneTags());
            makeupStyle = toCsv(b.getMakeupStyleTags());
            interestCategories = toCsvList(b.getInterestStyleTags());
            interestFunctions = toCsvList(b.getPrefferedFunctionTags());
        }

        // Contents
        String snsUrl = null;
        String avgVideoLength = null;
        String avgViews = null;
        String viewerGender = null;
        String viewerAge = null;
        String contentFormats = null;
        String contentTones = null;
        String desiredInvolvement = null;
        String desiredUsageScope = null;

        if (requestDto.getContent() != null) {
            var c = requestDto.getContent();

            contentFormats = toCsvList(c.getTypeTags());
            contentTones = toCsvList(c.getToneTags());
            desiredInvolvement = toCsvList(c.getPrefferedInvolvementTags());
            desiredUsageScope = toCsvList(c.getPrefferedCoverageTags());

            if (c.getSns() != null) {
                var sns = c.getSns();

                snsUrl = sns.getUrl();

                if (sns.getMainAudience() != null) {
                    viewerAge = toCsvList(sns.getMainAudience().getAgeTags());
                    viewerGender = toCsvList(sns.getMainAudience().getGenderTags());
                }
                if (sns.getAverageAudience() != null) {
                    avgVideoLength = toCsvList(sns.getAverageAudience().getVideoLengthTags());
                    avgViews = toCsvList(sns.getAverageAudience().getVideoViewsTags());
                }
            }
        }

        return UserMatchingDetail.builder()
                .userId(userId)
                .height(height)
                .bodyShape(bodyShape)
                .topSize(topSize)
                .bottomSize(bottomSize)
                .interestFields(interestFields)
                .interestStyles(interestStyles)
                .interestBrands(interestBrands)
                .skinType(skinType)
                .skinBrightness(skinBrightness)
                .makeupStyle(makeupStyle)
                .interestCategories(interestCategories)
                .interestFunctions(interestFunctions)
                .snsUrl(snsUrl)
                .avgVideoLength(avgVideoLength)
                .avgViews(avgViews)
                .viewerGender(viewerGender)
                .viewerAge(viewerAge)
                .contentFormats(contentFormats)
                .contentTones(contentTones)
                .desiredInvolvement(desiredInvolvement)
                .desiredUsageScope(desiredUsageScope)
                .build();
    }

    private static String toCsv(Integer tag) {
        return tag == null ? null : String.valueOf(tag);
    }

    private static String toCsvList(List<Integer> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
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
            String snsUrl,
            String viewerGender,
            String viewerAge,
            String avgVideoLength,
            String avgViews,
            String contentFormats,
            String contentTones,
            String desiredInvolvement,
            String desiredUsageScope
    ) {
        if (snsUrl != null) {
            this.snsUrl = snsUrl;
        }
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

    /**
     * 매칭 결과 설정 (검사 완료 후 호출)
     */
    public void setMatchingResult(String creatorType) {
        this.creatorType = creatorType;
    }

    /**
     * Soft delete 처리 (재검사 시 이전 데이터 폐기)
     */
    public void deprecated() {
        this.isDeprecated = true;
    }
}
