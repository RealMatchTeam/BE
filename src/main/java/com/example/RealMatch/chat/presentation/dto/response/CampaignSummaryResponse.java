package com.example.RealMatch.chat.presentation.dto.response;

public record CampaignSummaryResponse(
        Long campaignId,
        String campaignImageUrl,
        String brandName,
        String campaignTitle
) {
}
