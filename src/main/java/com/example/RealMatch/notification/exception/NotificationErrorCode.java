package com.example.RealMatch.notification.exception;

import org.springframework.http.HttpStatus;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {

    NOTIFICATION_INVALID_FILTER(HttpStatus.BAD_REQUEST, "NOTIFICATION_400_1", "유효하지 않은 필터 값입니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_404_1", "알림을 찾을 수 없습니다."),
    NOTIFICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "NOTIFICATION_403_1", "해당 알림에 대한 권한이 없습니다."),

    FCM_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "FCM_400_1", "유효하지 않은 FCM 토큰입니다."),
    FCM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM_500_1", "FCM 푸시 발송에 실패했습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_500_1", "이메일 발송에 실패했습니다."),
    DELIVERY_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DELIVERY_500_1", "알림 배달 처리에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
