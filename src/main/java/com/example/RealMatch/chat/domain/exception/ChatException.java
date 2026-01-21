package com.example.RealMatch.chat.domain.exception;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.Getter;

@Getter
public class ChatException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public ChatException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ChatException(BaseErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ChatException(BaseErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
