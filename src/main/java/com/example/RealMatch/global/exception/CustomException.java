package com.example.RealMatch.global.exception;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final BaseErrorCode code;

    public CustomException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode;
    }

    public CustomException(BaseErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode;
    }

    public CustomException(BaseErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode;
    }

    public CustomException(BaseErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode;
    }
}
