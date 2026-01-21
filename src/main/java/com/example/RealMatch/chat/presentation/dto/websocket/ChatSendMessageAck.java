package com.example.RealMatch.chat.presentation.dto.websocket;

import com.example.RealMatch.chat.presentation.dto.enums.ChatSendMessageAckStatus;

public record ChatSendMessageAck(
        String clientMessageId,
        Long messageId,
        ChatSendMessageAckStatus status
) {
}
