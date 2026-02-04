package com.example.RealMatch.chat.application.event.sender;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.exception.ChatRoomNotFoundException;
import com.example.RealMatch.chat.application.idempotency.ProcessedEventStore;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;

import lombok.RequiredArgsConstructor;

/**
 * 시스템 메시지 전송을 위한 멱등성 체크 및 오케스트레이션을 담당하는 컴포넌트.
 *
 * <p>중복 이벤트를 방지하기 위해 멱등성 키를 관리하며,
 * 실제 전송 및 재시도는 SystemMessageSender에 위임합니다.
 *
 * <p>sendWithIdempotency()의 반환값은 전송 성공 여부를 의미합니다.
 * - true: 전송 성공 및 markAsProcessed 완료 (at-least-once 보장)
 * - false: 중복 이벤트 또는 전송 실패
 *
 * <p>멱등성 예외(markIfNotProcessed/markAsProcessed 실패)는 스킵 금지.
 * 상위로 throw되어 BaseSystemMessageHandler.execute()에서 DLQ enqueue됩니다.
 */
@Component
@RequiredArgsConstructor
public class SystemMessageRetrySender {

    private static final Logger LOG = LoggerFactory.getLogger(SystemMessageRetrySender.class);

    private final ProcessedEventStore processedEventStore;
    private final SystemMessageSender systemMessageSender;

    private static final Duration EVENT_IDEMPOTENCY_TTL = Duration.ofHours(6);

    /**
     * 시스템 메시지를 멱등성 체크 후 전송합니다.
     *
     * <p>반환값은 전송 성공 여부를 의미합니다.
     * - true: 전송 성공 및 markAsProcessed 완료 (at-least-once 보장)
     * - false: 중복 이벤트 또는 전송 실패
     *
     * @return 전송 성공 여부
     */
    public boolean sendWithIdempotency(
            String idempotencyKey,
            Long roomId,
            ChatSystemMessageKind messageKind,
            ChatSystemMessagePayload payload,
            String eventType,
            Map<String, Object> additionalData
    ) {
        if (idempotencyKey == null) {
            throw new IllegalArgumentException("idempotencyKey cannot be null");
        }

        // 멱등성 체크: false=중복만, throw=Redis 장애 등 판단 불가 → 상위로 throw하여 DLQ 처리
        boolean isNewEvent = processedEventStore.markIfNotProcessed(idempotencyKey, EVENT_IDEMPOTENCY_TTL);
        if (!isNewEvent) {
            LOG.info("[RetrySender] Event already processed, skipping. key={}, eventType={}",
                    idempotencyKey, eventType);
            return false;
        }

        try {
            // 실제 전송 (재시도 가능) - SystemMessageSender에 위임
            systemMessageSender.sendWithRetry(idempotencyKey, roomId, messageKind, payload, eventType, additionalData);
        } catch (ChatRoomNotFoundException | IllegalArgumentException ex) {
            // 논리적 실패만 removeProcessed: 재시도해도 동일 결과이므로 키 제거 (레이스/중복 가능성 없음)
            processedEventStore.removeProcessed(idempotencyKey);
            LOG.error("[RetrySender] Logical failure (fallback - proxy inactive suspected). " +
                            "key={}, eventType={}, error={}. " +
                            "This should be handled by @Recover. Check Spring Retry proxy configuration.",
                    idempotencyKey, eventType, ex.getMessage(), ex);
            return false;
        } catch (Exception ex) {
            // 일시적 실패: removeProcessed 호출하지 않음 → 상위로 throw하여 DLQ 처리 (키 유지로 중복 전송 방지)
            LOG.error("[RetrySender] Unexpected exception from sendWithRetry (fallback - proxy inactive suspected). " +
                            "key={}, eventType={}. " +
                            "This should be handled by @Recover. Check Spring Retry proxy configuration.",
                    idempotencyKey, eventType, ex);
            throw ex;
        }

        // 전송 성공 후 markAsProcessed: 실패 시 throw → 상위에서 DLQ 처리 (성공처럼 넘기지 않음)
        processedEventStore.markAsProcessed(idempotencyKey, EVENT_IDEMPOTENCY_TTL);
        LOG.info("[RetrySender] System message sent successfully. key={}, roomId={}, kind={}",
                idempotencyKey, roomId, messageKind);
        return true;
    }
}
