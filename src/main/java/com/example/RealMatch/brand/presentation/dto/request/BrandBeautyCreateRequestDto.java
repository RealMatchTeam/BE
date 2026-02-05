package com.example.RealMatch.brand.presentation.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BrandBeautyCreateRequestDto {

    private String brandName;
    private String logoUrl;
    private String simpleIntro;
    private String detailIntro;
    private String homepageUrl;
    private List<String> brandDescriptionTags;
    private BrandTagsDto brandTags;

    @Getter
    @NoArgsConstructor
    public static class BrandTagsDto {
        private List<Integer> interestStyle;
        private List<Integer> interestFunction;
        private List<Integer> skinType;
        private List<Integer> makeupStyle;
    }
}
