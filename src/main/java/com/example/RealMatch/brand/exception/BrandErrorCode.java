package com.example.RealMatch.brand.exception;

import org.springframework.http.HttpStatus;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BrandErrorCode implements BaseErrorCode {

    BRAND_NOT_FOUND(HttpStatus.NOT_FOUND, "BRAND_404_1", "존재하지 않는 브랜드입니다."),
    BRAND_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "BRAND_400_1", "이미 해당 유저의 브랜드가 존재합니다."),
    INVALID_URL_FORMAT(HttpStatus.BAD_REQUEST, "BRAND_400_2", "유효하지 않은 URL 형식입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
