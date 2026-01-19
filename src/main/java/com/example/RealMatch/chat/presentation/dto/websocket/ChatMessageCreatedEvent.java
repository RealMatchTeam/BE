package com.example.RealMatch.chat.presentation.dto.websocket;

import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;

public record ChatMessageCreatedEvent(
        Long roomId,
        ChatMessageResponse message
) {
}
