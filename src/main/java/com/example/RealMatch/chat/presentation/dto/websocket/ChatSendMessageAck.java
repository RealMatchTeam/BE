package com.example.RealMatch.chat.presentation.dto.websocket;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSendMessageAckStatus;

public record ChatSendMessageAck(
        String clientMessageId,
        Long messageId,
        ChatSendMessageAckStatus status,
        String errorCode,
        String errorMessage
) {
    public static ChatSendMessageAck success(String clientMessageId, Long messageId) {
        return new ChatSendMessageAck(
                clientMessageId,
                messageId,
                ChatSendMessageAckStatus.SUCCESS,
                null,
                null
        );
    }

    public static ChatSendMessageAck failure(String clientMessageId, BaseErrorCode errorCode) {
        return new ChatSendMessageAck(
                clientMessageId,
                null,
                ChatSendMessageAckStatus.FAILED,
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }

    public static ChatSendMessageAck failure(String clientMessageId, String errorCode, String errorMessage) {
        return new ChatSendMessageAck(
                clientMessageId,
                null,
                ChatSendMessageAckStatus.FAILED,
                errorCode,
                errorMessage
        );
    }
}
