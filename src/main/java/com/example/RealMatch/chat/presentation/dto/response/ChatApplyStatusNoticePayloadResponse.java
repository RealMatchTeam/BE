package com.example.RealMatch.chat.presentation.dto.response;

import java.time.LocalDateTime;

import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;

public record ChatApplyStatusNoticePayloadResponse(
        Long applyId,
        Long actorUserId,
        LocalDateTime processedAt,
        ChatProposalStatus applyStatus
) implements ChatSystemMessagePayload {
}
