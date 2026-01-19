package com.example.RealMatch.chat.presentation.dto.websocket;

import com.example.RealMatch.chat.presentation.dto.enums.ChatMessageType;

import jakarta.validation.constraints.NotNull;

public record ChatSendMessageCommand(
        @NotNull Long roomId,
        @NotNull ChatMessageType messageType,
        String content,
        Long attachmentId,
        @NotNull String clientMessageId
) {
}
