package com.example.RealMatch.user.domain.exception;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.user.presentation.code.UserErrorCode;

import lombok.Getter;

@Getter
public class UserException extends CustomException {

    private final UserErrorCode errorCode;

    public UserException(UserErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
