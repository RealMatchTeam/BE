package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BrandRecruitingCampaignResponse {

    private List<CampaignCard> campaigns;

    @Getter
    @AllArgsConstructor
    public static class CampaignCard {
        private Long campaignId;
        private String brandName;
        private String title;
        private Integer recruitQuota;     // ex) 10명
        private Integer dDay;              // D-DAY = 0, D-3 = 3
        private Long rewardAmount;         // 원고료 (200000)
        private String imageUrl;            // 캠페인 썸네일 (또는 브랜드 로고)
    }
}
