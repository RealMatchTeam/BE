package com.example.RealMatch.chat.presentation.dto.response;

public record ChatMatchedCampaignPayloadResponse(
        Long campaignId,
        String campaignName,
        long amount,
        String currency,
        String orderNumber,
        String message
) implements ChatSystemMessagePayload {
}
