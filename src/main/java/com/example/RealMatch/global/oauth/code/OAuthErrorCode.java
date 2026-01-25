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
            "AUTH400_1",
            "소셜 로그인 시 이메일은 필수입니다."
    ),

    UNSUPPORTED_PROVIDER(
            HttpStatus.BAD_REQUEST,
            "AUTH400_2",
            "지원하지 않는 소셜 로그인입니다."
    ),

    NOT_REFRESH_TOKEN(
            HttpStatus.BAD_REQUEST,
            "AUTH400_3",
            "리프레시 토큰이 아닙니다."
    ),

    ALREADY_SIGNED_UP(
            HttpStatus.BAD_REQUEST,
            "AUTH400_4",
            "이미 회원가입이 완료된 유저입니다."
    ),


    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH401_1",
            "유효하지 않은 토큰입니다."
    ),

    TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH401_2",
            "토큰이 만료되었습니다."
    ),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "AUTH404_1",
            "인증 과정에서 해당 유저를 찾을 수 없습니다."
    ),

    TERM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "AUTH404_2",
            "약관 정보를 찾을 수 없습니다."
    ),

    PURPOSE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "AUTH404_3",
            "가입 목적 정보를 찾을 수 없습니다."
    ),

    CATEGORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "AUTH404_4",
            "콘텐츠 카테고리를 찾을 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
