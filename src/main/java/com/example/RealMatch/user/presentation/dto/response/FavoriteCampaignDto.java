package com.example.RealMatch.user.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FavoriteCampaignDto {

    private Long campaignId;
    private String campaignTitle;
    private Integer dDay;
    private Long rewardAmount;
    private Integer quota;

    private Long matchingRatio;     // user 기준
    private Long likeCount;

    private Long brandId;
    private String brandName;
    private String brandLogoUrl;
}

