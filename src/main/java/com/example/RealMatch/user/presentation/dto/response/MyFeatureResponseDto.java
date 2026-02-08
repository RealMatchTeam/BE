package com.example.RealMatch.user.presentation.dto.response;

import java.util.List;

public record MyFeatureResponseDto(
        BeautyType beautyType,
        FashionType fashionType,
        ContentsType contentsType
) {
    public record BeautyType(
            List<Integer> skinType,
            List<Integer> skinBrightness,
            List<Integer> makeupStyle,
            List<Integer> interestCategories,
            List<Integer> interestFunctions
    ) {}

    public record FashionType(
            List<Integer> height,
            List<Integer> bodyShape,
            List<Integer> topSize,
            List<Integer> bottomSize,
            List<Integer> interestFields,
            List<Integer> interestStyles,
            List<Integer> interestBrands
    ) {}

    public record ContentsType(
            List<Integer> viewerGender,
            List<Integer> viewerAge,
            List<Integer> avgVideoLength,
            List<Integer> avgViews,
            List<Integer> contentFormats,
            List<Integer> contentTones,
            List<Integer> desiredInvolvement,
            List<Integer> desiredUsageScope
    ) {}
}
