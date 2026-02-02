package com.example.RealMatch.oauth.code;

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

    REQUIRED_TERM_NOT_AGREED(
            HttpStatus.BAD_REQUEST,
            "AUTH400_5",
            "필수 약관에 동의하지 않았습니다."
    ),

    INVALID_NICKNAME(
            HttpStatus.BAD_REQUEST,
            "AUTH400_6",
            "닉네임은 필수 입력값입니다."
    ),

    INVALID_NICKNAME_LENGTH(
            HttpStatus.BAD_REQUEST,
            "AUTH400_7",
            "닉네임은 2~10자 사이여야 합니다."
    ),

    INVALID_NICKNAME_FORMAT(
            HttpStatus.BAD_REQUEST,
            "AUTH400_8",
            "닉네임은 한글, 영문, 숫자만 사용 가능합니다."
    ),

    FORBIDDEN_NICKNAME(
            HttpStatus.BAD_REQUEST,
            "AUTH400_9",
            "사용할 수 없는 닉네임입니다."
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
    ),

    DUPLICATE_NICKNAME(
            HttpStatus.CONFLICT,
            "AUTH409_1",
            "이미 사용 중인 닉네임입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
