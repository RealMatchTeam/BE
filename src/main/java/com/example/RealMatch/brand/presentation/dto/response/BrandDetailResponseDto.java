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
    private List<String> brandCategory;
    private BrandSkinCareTagDto brandSkinCareTag;
    private BrandMakeUpTagDto brandMakeUpTag;
    private BrandClothingTagDto brandClothingTag;

    @Getter
    @Builder
    public static class BrandSkinCareTagDto {
        private List<String> skinType;
        private List<String> mainFunction;
    }

    @Getter
    @Builder
    public static class BrandMakeUpTagDto {
        private List<String> brandMakeUpStyle;
    }

    @Getter
    @Builder
    public static class BrandClothingTagDto {
        private List<String> brandType;
        private List<String> brandStyle;
    }
}
