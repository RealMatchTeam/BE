package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandDetailResponseDto {
    private Long userId;
    private String brandName;
    private String logoUrl;
    private String simpleIntro;
    private String detailIntro;
    private String homepageUrl;
    private List<String> brandTag;
    private Integer brandMatchingRatio;
    private Boolean brandIsLiked;
    private List<String> brandDescriptionTags;

    private BrandBeautyResponse beautyResponse;
    private BrandFashionResponse fashionResponse;

    @Getter
    @Builder
    public static class BrandBeautyResponse {
        private List<String> categories; // 스킨케어, 메이크업, 바디 등
        private List<String> skinType;   // 스킨케어/바디 공통
        private List<String> mainFunction; // 스킨케어/바디 공통 (관심기능)
        private List<String> makeUpStyle;  // 메이크업 전용
    }

    @Getter
    @Builder
    public static class BrandFashionResponse {
        private List<String> categories; // 의류, 가방, 신발, 주얼리, 패션 소품 등
        private List<String> brandType;  // 브랜드 종류
        private List<String> brandStyle; // 브랜드 스타일
    }
}
