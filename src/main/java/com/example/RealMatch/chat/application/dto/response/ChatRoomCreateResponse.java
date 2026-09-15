package com.example.RealMatch.chat.application.dto.response;

import java.time.LocalDateTime;

public record ChatRoomCreateResponse(
        Long roomId,
        String roomKey,
        LocalDateTime createdAt
) {
}
