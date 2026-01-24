package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandDetailViewResponseDto {

    private String brandName;
    private List<String> brandTag;
    private String brandDescription;
    private Integer brandMatchingRatio;
    private Boolean brandIsLiked;
    private List<String> brandCategory;
    private BrandSkinCareTagDto brandSkinCareTag;
    private BrandMakeUpTagDto brandMakeUpTag;
    private List<OnGoingCampaignDto> brandOnGoingCampaign;
    private List<AvailableSponsorProdDto> availableSponsorProd;
    private List<CampaignHistoryDto> campaignHistory;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandSkinCareTagDto {
        private List<String> brandSkinType;
        private List<String> brandMainFunction;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandMakeUpTagDto {
        private List<String> brandSkinType;
        private List<String> brandMakeUpStyle;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnGoingCampaignDto {
        private Long brandId;
        private String brandName;
        private Integer recruitingTotalNumber;
        private Integer recruitedNumber;
        private String campaignDescription;
        private String campaignManuscriptFee;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableSponsorProdDto {
        private Long productId;
        private String productName;
        private String availableType;
        private Integer availableQuantity;
        private Integer availableSize;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignHistoryDto {
        private Long campaignId;
        private String campaignTitle;
        private String startDate;
        private String endDate;
    }
}
