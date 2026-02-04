package com.example.RealMatch.chat.application.event.sender;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.idempotency.FailedEventDlq;
import com.example.RealMatch.chat.application.idempotency.ProcessedEventStore;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;

import lombok.RequiredArgsConstructor;

/**
 * 시스템 메시지 전송 실패 처리 구현체.
 *
 * <p>전송 실패 시 Redis 키 삭제 및 DLQ 기록을 수행합니다.
 */
@Component
@RequiredArgsConstructor
public class SystemMessageFailureHandlerImpl implements SystemMessageFailureHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SystemMessageFailureHandlerImpl.class);

    private final ProcessedEventStore processedEventStore;
    private final FailedEventDlq failedEventDlq;

    @Override
    public void handleFailure(
            String idempotencyKey,
            Long roomId,
            ChatSystemMessageKind messageKind,
            String eventType,
            String errorMessage,
            Map<String, Object> additionalData
    ) {
        // 전송 실패 시 Redis 키 삭제 (재시도 가능하도록)
        // 예외가 발생해도 전체 흐름을 막지 않도록 try-catch로 보호
        try {
            processedEventStore.removeProcessed(idempotencyKey);
            LOG.debug("[FailureHandler] Removed processed event key. key={}, eventType={}",
                    idempotencyKey, eventType);
        } catch (Exception ex) {
            LOG.error("[FailureHandler] Failed to remove processed event key. key={}, eventType={}",
                    idempotencyKey, eventType, ex);
            // 예외를 다시 던지지 않음 (DLQ 기록은 계속 진행)
        }

        LOG.error("[FailureHandler] Handling system message failure. " +
                        "key={}, roomId={}, kind={}, eventType={}",
                idempotencyKey, roomId, messageKind, eventType);

        // DLQ 기록
        // 예외가 발생해도 전체 흐름을 막지 않도록 try-catch로 보호
        try {
            Map<String, Object> dlqData = new HashMap<>(additionalData != null ? additionalData : Map.of());
            dlqData.put("messageKind", messageKind != null ? messageKind.toString() : "UNKNOWN");

            failedEventDlq.enqueueFailedEvent(
                    eventType,
                    idempotencyKey,
                    roomId,
                    errorMessage,
                    dlqData
            );
        } catch (Exception ex) {
            LOG.error("[FailureHandler] Failed to enqueue failed event to DLQ. key={}, eventType={}",
                    idempotencyKey, eventType, ex);
            // 예외를 다시 던지지 않음 (이미 로그는 남겼음)
        }
    }
}
