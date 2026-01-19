package com.example.RealMatch.chat.presentation.dto.response;

import java.time.LocalDateTime;

public record ChatProposalStatusNoticePayloadResponse(
        Long proposalId,
        Long actorUserId,
        LocalDateTime processedAt
) implements ChatSystemMessagePayload {
}
