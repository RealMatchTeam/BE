package com.example.RealMatch.chat.presentation.dto.response;

import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;
import com.example.RealMatch.chat.presentation.dto.enums.ChatProposalDecisionStatus;

public record ChatProposalCardPayloadResponse(
        Long proposalId,
        Long campaignId,
        String campaignName,
        String campaignSummary,
        ChatProposalDecisionStatus proposalStatus,
        ChatProposalDirection proposalDirection
) implements ChatSystemMessagePayload {
}
