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
public class MatchCampaignResponseDto {

    private List<CampaignDto> brands;
    private Integer count;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignDto {
        private Long brandId;
        private String brandName;
        private String brandLogoUrl;
        private Integer brandMatchingRatio;
        private Boolean brandIsLiked;
        private Boolean brandIsRecruiting;
        private Long campaignId;
        private Integer campaignManuscriptFee;
        private String campaignName;   // 캠페인명 (keyword 검색 대상)
        private Integer campaignDDay;
        private Boolean campaignIsLiked;
        private Integer campaignTotalRecruit;
        private Integer campaignTotalCurrentRecruit;
    }

    public static MatchCampaignResponseDto empty() {
        return MatchCampaignResponseDto.builder()
                .brands(List.of())
                .count(0)
                .build();
    }
}
