package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandDetailResponseDto {
    private String brandName;
    private List<String> brandTag;
    private String brandDescription;
    private Integer brandMatchingRatio;
    private Boolean brandIsLiked;
    private List<String> brandCategory;
    private BrandSkinCareTagDto brandSkinCareTag;
    private BrandMakeUpTagDto brandMakeUpTag;
    private List<BrandOnGoingCampaignDto> brandOnGoingCampaign;
    private List<AvailableSponsorProdDto> availableSponsorProd;
    private List<CampaignHistoryDto> campaignHistory;

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

    @Getter
    @Builder
    public static class BrandOnGoingCampaignDto {
        private Long brandId;
        private String brandName;
        private Integer recruitingTotalNumber;
        private Integer recruitedNumber;
        private String campaignDescription;
        private String campaginManuscriptFee;
    }

    @Getter
    @Builder
    public static class AvailableSponsorProdDto {
        private Long productId;
        private String productName;
        private String availableType;
        private Integer availableQuantity;
        private Integer availableSize;
    }

    @Getter
    @Builder
    public static class CampaignHistoryDto {
        private Long campaignId;
        private String campaignTitle;
        private String startDate;
        private String endDate;
    }
}
