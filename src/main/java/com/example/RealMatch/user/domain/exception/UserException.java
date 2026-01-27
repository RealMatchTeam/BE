package com.example.RealMatch.user.domain.exception;

import com.example.RealMatch.user.presentation.code.UserErrorCode;

import lombok.Getter;

@Getter
public class UserException extends RuntimeException {

    private final UserErrorCode errorCode;

    public UserException(UserErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
