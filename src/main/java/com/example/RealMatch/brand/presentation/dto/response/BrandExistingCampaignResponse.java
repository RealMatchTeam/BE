package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BrandExistingCampaignResponse {

    private List<CampaignItem> campaigns;

    @Getter
    @AllArgsConstructor
    public static class CampaignItem {
        private Long campaignId;
        private String title;
    }
}
