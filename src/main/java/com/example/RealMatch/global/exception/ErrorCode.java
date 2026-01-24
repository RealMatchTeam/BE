package com.example.RealMatch.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "C001", "잘못된 파라미터입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    // Brand
    BRAND_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "해당 브랜드를 찾을 수 없습니다."),
    INVALID_DOMAIN(HttpStatus.BAD_REQUEST, "B002", "유효하지 않은 도메인입니다. (BEAUTY 또는 FASHION만 가능)");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
