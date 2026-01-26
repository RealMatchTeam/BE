package com.example.RealMatch.chat.presentation.dto.response;

import java.time.LocalDateTime;

public record ChatRoomCreateResponse(
        Long roomId,
        String roomKey,
        LocalDateTime createdAt
) {
}
