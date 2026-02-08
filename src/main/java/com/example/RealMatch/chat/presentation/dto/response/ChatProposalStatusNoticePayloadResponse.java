package com.example.RealMatch.chat.presentation.dto.response;

import java.time.LocalDateTime;

import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;

public record ChatProposalStatusNoticePayloadResponse(
        Long proposalId,
        Long actorUserId,
        LocalDateTime processedAt,
        ChatProposalStatus proposalStatus
) implements ChatSystemMessagePayload {
}
