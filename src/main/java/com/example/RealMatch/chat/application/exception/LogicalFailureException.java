package com.example.RealMatch.chat.application.exception;

/**
 * 논리적 실패를 나타내는 예외.
 *
 * <p>이 예외는 재시도해도 동일한 결과가 나올 수 있는 논리적 오류를 나타냅니다.
 * 예: ChatRoomNotFoundException, IllegalArgumentException 등
 *
 * <p>SystemMessageRetrySender는 이 예외를 catch하여:
 * - removeProcessed()로 멱등성 키를 제거하고
 * - false를 반환하여 실패를 명시적으로 처리합니다.
 *
 * <p>이 예외는 DLQ에 기록되지 않으며, 정책상 무시되는 실패입니다.
 */
public class LogicalFailureException extends RuntimeException {

    public LogicalFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
