package com.example.RealMatch.user.presentation.dto.response;

import java.util.List;

public record MyFeatureResponseDto(
        String creatorType,
        BeautyType beautyType,
        FashionType fashionType,
        ContentsType contentsType
) {
    public record BeautyType(
            List<String> skinType,
            String skinBrightness,
            List<String> makeupStyle,
            List<String> interestCategories,
            List<String> interestFunctions
    ) {}

    public record FashionType(
            String height,
            String bodyShape,
            String topSize,
            String bottomSize,
            List<String> interestFields,
            List<String> interestStyles,
            List<String> interestBrands
    ) {}

    public record ContentsType(
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
