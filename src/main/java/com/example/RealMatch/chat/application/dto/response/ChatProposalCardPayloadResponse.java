package com.example.RealMatch.chat.application.dto.response;

import com.example.RealMatch.chat.application.dto.enums.ChatProposalDecisionStatus;
import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;

public record ChatProposalCardPayloadResponse(
        Long proposalId,
        Long campaignId,
        String campaignName,
        String campaignSummary,
        ChatProposalDecisionStatus proposalStatus,
        ChatProposalDirection proposalDirection
) implements ChatSystemMessagePayload {
}
