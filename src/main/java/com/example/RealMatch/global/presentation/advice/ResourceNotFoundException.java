package com.example.RealMatch.global.presentation.advice;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
    }
}
