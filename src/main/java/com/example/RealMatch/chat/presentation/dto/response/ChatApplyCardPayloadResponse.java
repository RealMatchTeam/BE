package com.example.RealMatch.chat.presentation.dto.response;

public record ChatApplyCardPayloadResponse(
        Long applyId,
        Long campaignId,
        String campaignName,
        String campaignDescription,
        String applyReason
) implements ChatSystemMessagePayload {
}
