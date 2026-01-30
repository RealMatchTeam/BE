package com.example.RealMatch.chat.application.util;

import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.global.exception.CustomException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatExceptionConverter {

    public static CustomException convert(IllegalArgumentException ex) {
        return new CustomException(ChatErrorCode.INVALID_MESSAGE_FORMAT, ex.getMessage());
    }

    public static CustomException convert(IllegalArgumentException ex, String customMessage) {
        return new CustomException(ChatErrorCode.INVALID_MESSAGE_FORMAT, customMessage);
    }
}
