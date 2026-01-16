package com.example.RealMatch.global.presentation.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST,
            "COMMON400_1",
            "잘못된 요청입니다."),

    INVALID_DATA(HttpStatus.BAD_REQUEST,
            "COMMON4001_2",
            "유효하지 않은 데이터입니다."),

    INVALID_PAGE(HttpStatus.BAD_REQUEST,
            "COMMON400_3",
            "페이지 번호는 1 이상이어야 합니다."),

    DUPLICATE_MISSION(HttpStatus.BAD_REQUEST,
            "COMMON400_4",
            "이미 도전 중인 미션입니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
            "COMMON401_1",
            "인증이 필요합니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN,
            "COMMON403_1",
            "접근 권한이 없습니다."),

    NOT_FOUND(HttpStatus.NOT_FOUND,
            "COMMON404_1",
            "요청한 리소스를 찾을 수 없습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "COMMON500_1",
            "서버 내부 오류가 발생했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
