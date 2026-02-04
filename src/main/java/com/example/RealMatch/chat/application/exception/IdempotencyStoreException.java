package com.example.RealMatch.chat.application.exception;

/**
 * 멱등성 저장소(Redis 등) 작업 실패 시 발생하는 예외.
 *
 * <p>Redis 장애, 응답 null, 타임아웃 등 "판단 불가" 상황에서 발생하며,
 * 호출자는 이를 실패로 처리(DLQ/재시도/에러)해야 합니다.
 * false(중복)와 구분하여 반드시 예외로 전파됩니다.
 */
public class IdempotencyStoreException extends RuntimeException {

    public IdempotencyStoreException(String message) {
        super(message);
    }

    public IdempotencyStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
