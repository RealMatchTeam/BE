package com.example.RealMatch.global.oauth.exception;

import com.example.RealMatch.global.oauth.code.OAuthErrorCode;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private final OAuthErrorCode errorCode;

    public AuthException(OAuthErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
