package com.example.RealMatch.chat.application.event.sender;

import java.util.Map;

import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;

/**
 * 시스템 메시지 전송 실패 시 처리 정책을 담당하는 인터페이스.
 *
 * <p>전송 실패 시 Redis 키 정리 및 DLQ 기록 등의 정책을 처리합니다.
 */
public interface SystemMessageFailureHandler {

    /**
     * 시스템 메시지 전송 실패를 처리합니다.
     */
    void handleFailure(
            String idempotencyKey,
            Long roomId,
            ChatSystemMessageKind messageKind,
            String eventType,
            String errorMessage,
            Map<String, Object> additionalData
    );
}
