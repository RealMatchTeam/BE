package com.example.RealMatch.chat.application.dto.websocket;

import com.example.RealMatch.chat.application.dto.response.ChatMessageResponse;

public record ChatMessageCreatedEvent(
        Long roomId,
        ChatMessageResponse message
) {
}
