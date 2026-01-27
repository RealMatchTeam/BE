package com.example.RealMatch.oauth.exception;

import com.example.RealMatch.oauth.code.OAuthErrorCode;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private final OAuthErrorCode errorCode;

    public AuthException(OAuthErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
