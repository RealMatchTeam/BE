package com.example.RealMatch.brand.presentation.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BrandUpdateRequestDto {
    // 기본 정보
    private String brandName;
    private String logoUrl;
    private String simpleIntro;
    private String detailIntro;
    private String homepageUrl;

    // 카테고리
    private List<String> brandCategory;

    // 조건부 태그 객체들
    private BrandSkinCareTagDto brandSkinCareTag;
    private BrandMakeUpTagDto brandMakeUpTag;
    private BrandClothingTagDto brandClothingTag;

    // --- 중첩 클래스 정의 ---
    @Getter
    @NoArgsConstructor
    public static class BrandSkinCareTagDto {
        private List<String> skinType;
        private List<String> mainFunction;
    }

    @Getter
    @NoArgsConstructor
    public static class BrandMakeUpTagDto {
        private List<String> skinType;
        private List<String> brandMakeUpStyle;
    }

    @Getter
    @NoArgsConstructor
    public static class BrandClothingTagDto {
        private List<String> brandType;
        private List<String> brandStyle;
    }
}
