package com.example.RealMatch.chat.presentation.dto.response;

import java.util.List;

public record CampaignSummaryResponse(
        Long campaignId,
        String campaignTitle,
        List<CampaignSummarySponsorProductResponse> sponsorProducts
) {

    public record CampaignSummarySponsorProductResponse(
            Long productId,
            String productName,
            String thumbnailImageUrl
    ) {}
}
