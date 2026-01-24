package com.example.RealMatch.brand.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandDetailResponseDto {
    private BrandInfo brandInfo;
    private CategoryTags categoryTags;
    private List<CampaignDto> campaigns;
    private List<SponsoredProductDto> sponsoredProducts;
    private List<HistoryDto> history;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandInfo {
        private Long brandId;
        private String nameKr;
        private Integer matchRate;
        private String description;
        private String mainImageUrl;
        private String logoUrl;
        private List<String> tags;
        private Boolean isLiked;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryTags {
        private List<String> mainCategories;
        private List<DetailTag> detailTags;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailTag {
        private String category;
        private List<String> tags;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignDto {
        private Long campaignId;
        private Integer dDay;
        private String title;
        private Integer currentRecruit;
        private Integer totalRecruit;
        private Integer rewardAmount;
        private Boolean isLiked;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SponsoredProductDto {
        private Long productId;
        private String name;
        private String imageUrl;
        private String description;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryDto {
        private String date;
        private String content;
        private String status;
    }
}
