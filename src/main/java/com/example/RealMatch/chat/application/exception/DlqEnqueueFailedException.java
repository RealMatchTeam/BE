package com.example.RealMatch.chat.application.exception;

/**
 * @Recover에서 DLQ enqueue가 실패했음을 알리는 예외.
 *
 * <p>BaseSystemMessageHandler는 이 예외(또는 cause 체인에 포함된 경우)를 catch 시
 * fallback DLQ enqueue를 수행합니다.
 * DlqEnqueuedException("이미 DLQ 넣음")과 구분하여 "DLQ 못 넣음 → fallback 필요"를 명시합니다.
 */
public class DlqEnqueueFailedException extends RuntimeException {

    public DlqEnqueueFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message   메시지
     * @param cause     원래 전송 실패 예외
     * @param suppressed DLQ enqueue 실패 예외 (failureHandler에서 던진 예외)
     */
    public DlqEnqueueFailedException(String message, Throwable cause, Throwable suppressed) {
        super(message, cause);
        if (suppressed != null) {
            addSuppressed(suppressed);
        }
    }
}
