package com.example.RealMatch.chat.presentation.dto.response;

import java.time.LocalDateTime;

public record ChatApplyStatusNoticePayloadResponse(
        Long applyId,
        Long actorUserId,
        LocalDateTime processedAt
) implements ChatSystemMessagePayload {
}
