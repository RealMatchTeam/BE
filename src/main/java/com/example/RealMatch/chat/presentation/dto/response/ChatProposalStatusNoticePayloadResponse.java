package com.example.RealMatch.chat.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatProposalStatusNoticePayloadResponse(
        UUID proposalId,
        Long actorUserId,
        LocalDateTime processedAt
) implements ChatSystemMessagePayload {
}
