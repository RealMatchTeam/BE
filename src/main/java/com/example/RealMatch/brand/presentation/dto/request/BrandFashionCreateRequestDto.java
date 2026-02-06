package com.example.RealMatch.brand.presentation.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BrandFashionCreateRequestDto {

    private String brandName;
    private List<String> brandImages;
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
        private List<Integer> interestItem;
        private List<Integer> interestBrand;
    }
}
