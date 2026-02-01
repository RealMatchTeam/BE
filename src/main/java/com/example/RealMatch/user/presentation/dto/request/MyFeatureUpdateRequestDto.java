package com.example.RealMatch.user.presentation.dto.request;

import java.util.List;

public record MyFeatureUpdateRequestDto(
        BeautyTypeUpdate beautyType,
        FashionTypeUpdate fashionType,
        ContentsTypeUpdate contentsType
) {
    public record BeautyTypeUpdate(
            List<String> skinType,
            String skinBrightness,
            List<String> makeupStyle,
            List<String> interestCategories,
            List<String> interestFunctions
    ) {}

    public record FashionTypeUpdate(
            String bodyStats,
            String bodyShape,
            String topSize,
            String bottomSize,
            List<String> interestFields,
            List<String> interestStyles,
            List<String> interestBrands
    ) {}

    public record ContentsTypeUpdate(
            List<String> viewerGender,
            List<String> viewerAge,
            String avgVideoLength,
            String avgViews,
            List<String> contentFormats,
            List<String> contentTones,
            List<String> desiredInvolvement,
            List<String> desiredUsageScope
    ) {}
}
