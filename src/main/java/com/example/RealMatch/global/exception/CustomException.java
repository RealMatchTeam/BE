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
}
