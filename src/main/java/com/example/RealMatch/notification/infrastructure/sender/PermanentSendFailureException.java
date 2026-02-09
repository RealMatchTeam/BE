package com.example.RealMatch.notification.infrastructure.sender;

/**
 * 재시도가 불가능한 영구 발송 실패를 나타내는 예외.
 * 예: 유효하지 않은 FCM 토큰, 존재하지 않는 이메일 주소 등.
 */
public class PermanentSendFailureException extends RuntimeException {

    public PermanentSendFailureException(String message) {
        super(message);
    }

    public PermanentSendFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
