package com.example.RealMatch.chat.application.util;

import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatExceptionConverter {

    public static ChatException convert(IllegalArgumentException ex) {
        return new ChatException(ChatErrorCode.INVALID_MESSAGE_FORMAT, ex.getMessage());
    }

    public static ChatException convert(IllegalArgumentException ex, String customMessage) {
        return new ChatException(ChatErrorCode.INVALID_MESSAGE_FORMAT, customMessage);
    }
}
