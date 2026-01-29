package com.example.RealMatch.oauth.exception;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.oauth.code.OAuthErrorCode;

import lombok.Getter;

@Getter
public class AuthException extends CustomException {

    private final OAuthErrorCode errorCode;

    public AuthException(OAuthErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
