package com.example.RealMatch.chat.application.dto.response;

import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;

public record ChatMatchedCampaignPayloadResponse(
        Long campaignId,
        String campaignName,
        long amount,
        String currency,
        String orderNumber,
        String message,
        ChatProposalDirection proposalDirection
) implements ChatSystemMessagePayload {
}
