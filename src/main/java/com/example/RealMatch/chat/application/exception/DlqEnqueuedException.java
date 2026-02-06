package com.example.RealMatch.chat.application.exception;

/**
 * 이미 @Recover에서 DLQ 기록이 완료되었음을 알리는 예외.
 *
 * <p>BaseSystemMessageHandler는 이 예외를 catch 시 DLQ에 다시 넣지 않고 return합니다.
 * 재시도 소진 후 실패 시 DLQ 중복 기록을 방지합니다.
 */
public class DlqEnqueuedException extends RuntimeException {

    public DlqEnqueuedException(String message, Throwable cause) {
        super(message, cause);
    }
}
