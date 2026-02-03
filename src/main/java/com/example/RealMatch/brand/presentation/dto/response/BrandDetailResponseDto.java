package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandDetailResponseDto {
    private Long userId;
    private String brandName;
    private List<String> brandTag;
    private String brandDescription;
    private Integer brandMatchingRatio;
    private Boolean brandIsLiked;
    private List<String> brandCategory;
    private BrandSkinCareTagDto brandSkinCareTag;
    private BrandMakeUpTagDto brandMakeUpTag;

    @Getter
    @Builder
    public static class BrandSkinCareTagDto {
        private List<String> brandSkinType;
        private List<String> brandMainFunction;
    }

    @Getter
    @Builder
    public static class BrandMakeUpTagDto {
        private List<String> brandSkinType;
        private List<String> brandMakeUpStyle;
    }
}
