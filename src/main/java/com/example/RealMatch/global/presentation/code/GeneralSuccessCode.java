package com.example.RealMatch.global.presentation.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GeneralSuccessCode implements BaseSuccessCode {

    GOOD_REQUEST(HttpStatus.OK,
            "COMMON200_1",
            "정상적인 요청입니다."),
    AUTHORIZED(HttpStatus.CREATED,
            "AUTH201_1",
            "인증이 확인되었습니다."),
    CREATE(HttpStatus.CREATED,
            "CREATE200_1",
            "성공적으로 생성되었습니다."),
    ALLOWED(HttpStatus.ACCEPTED,
            "AUTH203_1",
            "요청이 허용되었습니다."),
    FOUND(HttpStatus.FOUND,
            "COMMON302_1",
            "요청한 리소스를 찾았습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
