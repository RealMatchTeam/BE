package com.example.RealMatch.chat.application.dto.response;

public record ChatApplyCardPayloadResponse(
        Long applyId,
        Long campaignId,
        String campaignName,
        String campaignDescription,
        String applyReason
) implements ChatSystemMessagePayload {
}
