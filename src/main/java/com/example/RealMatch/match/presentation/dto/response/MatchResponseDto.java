package com.example.RealMatch.match.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDto {

    private CreatorAnalysisDto creatorAnalysis;
    private HighMatchingBrandListDto highMatchingBrandList;
    private HighMatchingCampaignListDto highMatchingCampaignList;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatorAnalysisDto {
        private String creatorType;
        private String beautyStyle;
        private String fashionStyle;
        private String contentStyle;
        private String bestFitBrand;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighMatchingBrandListDto {
        private Integer count;
        private List<BrandDto> brands;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandDto {
        private Long id;
        private String name;
        private Integer matchingRatio;
        private Boolean isLiked;
        private Boolean isRecruiting;
        private List<String> tags;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighMatchingCampaignListDto {
        private Integer count;
        private List<CampaignDto> brands;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignDto {
        private Long id;
        private String name;
        private Integer matchingRatio;
        private Boolean isLiked;
        private Boolean isRecruiting;
        private Integer manuscriptFee;
        private String detail;
        private Integer dDay;
        private Integer totalRecruit;
        private Integer currentRecruit;
    }
}
