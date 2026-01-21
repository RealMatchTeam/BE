package com.example.RealMatch.chat.presentation.dto.websocket;

import com.example.RealMatch.chat.presentation.dto.enums.ChatMessageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatSendMessageCommand(
        @NotNull Long roomId,
        @NotNull ChatMessageType messageType,
        @Size(max = 5000) String content,
        Long attachmentId,
        @NotBlank String clientMessageId
) {
}
