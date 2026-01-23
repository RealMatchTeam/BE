package com.example.RealMatch.global.oauth.code;

import org.springframework.http.HttpStatus;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OAuthErrorCode implements BaseErrorCode {

    EMAIL_NOT_PROVIDED(
            HttpStatus.BAD_REQUEST,
            "OAUTH400_1",
            "소셜 로그인 시 이메일은 필수입니다."
    ),

    UNSUPPORTED_PROVIDER(
            HttpStatus.BAD_REQUEST,
            "OAUTH400_2",
            "지원하지 않는 소셜 로그인입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
