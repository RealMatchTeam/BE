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

    private String name;
    private List<String> tags;
    private String description;
    private Integer matchRate;
    private Boolean isLiked;
    private List<String> categories;
    private BrandSkinCareTagDto skinCareTags;
    private BrandMakeUpTagDto makeUpTags;
    private List<OnGoingCampaignDto> onGoingCampaigns;
    private List<AvailableSponsorProdDto> availableSponsorProducts;
    private List<CampaignHistoryDto> campaignHistories;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandSkinCareTagDto {
        private List<String> skinTypes;
        private List<String> mainFunctions;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandMakeUpTagDto {
        private List<String> skinTypes;
        private List<String> makeUpStyles;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnGoingCampaignDto {
        private Long campaignId;
        private String campaignName;
        private Integer recruitingTotalNumber;
        private Integer recruitedNumber;
        private String description;
        private String manuscriptFee;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableSponsorProdDto {
        private Long productId;
        private String productName;
        private String type;
        private Integer quantity;
        private Integer size;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignHistoryDto {
        private Long campaignId;
        private String title;
        private String startDate;
        private String endDate;
    }
}
