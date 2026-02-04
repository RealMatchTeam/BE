package com.example.RealMatch.chat.application.event;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import com.example.RealMatch.chat.application.exception.ChatRoomNotFoundException;
import com.example.RealMatch.chat.application.idempotency.FailedEventDlq;
import com.example.RealMatch.chat.application.idempotency.ProcessedEventStore;

import lombok.RequiredArgsConstructor;

/**
 * 시스템 메시지 핸들러의 공통 로직을 제공하는 추상 클래스
 */
@RequiredArgsConstructor
public abstract class BaseSystemMessageHandler {

    protected final ProcessedEventStore processedEventStore;
    protected final FailedEventDlq failedEventDlq;

    protected static final Duration EVENT_IDEMPOTENCY_TTL = Duration.ofHours(6);

    /**
     * 이벤트 타입 이름을 반환합니다 (로깅 및 DLQ용).
     */
    protected abstract String getEventType();

    /**
     * 로거를 반환합니다.
     */
    protected abstract Logger getLogger();

    /**
     * 이벤트 중복 처리 검증 (Redis 기반)
     * 
     * <p>처리 흐름:
     * <ol>
     *   <li>markIfNotProcessed(): SETNX로 처리 시작 시점에 키 생성 (중복 체크)</li>
     *   <li>전송 성공 시: markAsProcessed()로 키를 확실히 남김 (SET 명령어)</li>
     *   <li>논리적 실패 시: removeProcessed()로 키 삭제</li>
     *   <li>최종 실패 시: @Recover에서 removeProcessed()로 키 삭제</li>
     * </ol>
     */
    protected boolean checkIfNotProcessed(String eventId) {
        if (eventId == null) {
            getLogger().error("[{}] {} has null eventId, this should never happen. Rejecting event.",
                    getEventType(), getEventType());
            throw new IllegalStateException("Event ID cannot be null for " + getEventType());
        }

        // SETNX로 처리 시작 시점에 키 생성 (중복 체크)
        boolean isNewEvent = processedEventStore.markIfNotProcessed(eventId, EVENT_IDEMPOTENCY_TTL);
        if (!isNewEvent) {
            getLogger().warn("[{}] Duplicate {} detected, skipping. eventId={}",
                    getEventType(), getEventType(), eventId);
            return false;
        }

        return true;
    }

    /**
     * 논리적 실패 처리 (Redis 키 삭제)
     */
    protected void handleLogicalFailure(String eventId, String reason) {
        getLogger().warn("[{}] Logical failure: {}. Removing Redis key. eventId={}",
                getEventType(), reason, eventId);
        processedEventStore.removeProcessed(eventId);
    }

    /**
     * 전송 성공 처리 (Redis 키 확실히 남김)
     */
    protected void markAsProcessed(String eventId) {
        processedEventStore.markAsProcessed(eventId, EVENT_IDEMPOTENCY_TTL);
    }

    /**
     * 전송 실패 복구 처리 (공통)
     */
    protected void recoverFailure(
            Exception ex,
            String eventId,
            Long roomId,
            String operation,
            Map<String, Object> additionalData
    ) {
        // 전송 실패 시 Redis 키 삭제 (재시도 가능하도록)
        processedEventStore.removeProcessed(eventId);
        getLogger().error("[{}] Failed to {} after all retries (3 attempts). " +
                        "eventId={}, roomId={}, Redis key removed",
                getEventType(), operation, eventId, roomId, ex);

        Map<String, Object> dlqData = new HashMap<>(additionalData != null ? additionalData : Map.of());
        failedEventDlq.enqueueFailedEvent(
                getEventType(),
                eventId,
                roomId,
                ex.getMessage(),
                dlqData
        );
    }

    /**
     * 재시도 가능한 메시지 전송 메서드에 적용할 공통 Retryable 설정
     */
    @Retryable(
            retryFor = {Exception.class},
            noRetryFor = {ChatRoomNotFoundException.class, IllegalArgumentException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 800)
    )
    protected void retryableSend(Runnable sendAction, String eventId, String operation) {
        try {
            getLogger().info("[{}] Attempting to {}. eventId={}", getEventType(), operation, eventId);
            sendAction.run();
            markAsProcessed(eventId);
            getLogger().info("[{}] {} completed successfully. eventId={}", getEventType(), operation, eventId);
        } catch (Exception ex) {
            // 재시도 가능한 예외는 Spring Retry가 처리
            // 최종 실패는 @Recover에서 키 삭제
            getLogger().warn("[{}] Failed to {} (will retry). eventId={}, error={}",
                    getEventType(), operation, eventId, ex.getMessage());
            throw ex;
        }
    }
}
