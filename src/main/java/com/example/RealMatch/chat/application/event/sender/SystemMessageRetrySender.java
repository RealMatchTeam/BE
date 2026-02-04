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
 * <p>성공/실패 관측 기준:
 * - 선점: markIfNotProcessed() (SETNX)
 * - 성공 확정: markAsProcessed() (SET)
 * - 실패: FailureHandler.removeProcessed() + DLQ
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

        // 멱등성 체크: 이미 처리된 이벤트면 skip
        boolean isNewEvent = processedEventStore.markIfNotProcessed(idempotencyKey, EVENT_IDEMPOTENCY_TTL);
        if (!isNewEvent) {
            LOG.info("[RetrySender] Event already processed, skipping. key={}, eventType={}",
                    idempotencyKey, eventType);
            return false;
        }

        try {
            // 실제 전송 (재시도 가능) - SystemMessageSender에 위임
            systemMessageSender.sendWithRetry(idempotencyKey, roomId, messageKind, payload, eventType, additionalData);
            
            // 전송 성공 시 Redis 키를 확실히 남김 (정책: RetrySender가 담당)
            // 성공 확정: markAsProcessed()로 at-least-once 보장
            processedEventStore.markAsProcessed(idempotencyKey, EVENT_IDEMPOTENCY_TTL);
            LOG.info("[RetrySender] System message sent successfully. key={}, roomId={}, kind={}",
                    idempotencyKey, roomId, messageKind);
            
            return true; // success: 전송 성공 및 markAsProcessed 완료
        } catch (ChatRoomNotFoundException | IllegalArgumentException ex) {
            // 논리적 실패 fallback 처리
            // 일반적으로 SystemMessageSender의 @Recover에서 처리되지만,
            // Spring Retry 프록시 미적용 등의 엣지 케이스 대비 fallback
            // 선점 키를 삭제하여 재시도/재처리 가능 상태로 복구
            processedEventStore.removeProcessed(idempotencyKey);
            LOG.error("[RetrySender] Logical failure (fallback - proxy inactive suspected). " +
                            "key={}, eventType={}, error={}. " +
                            "This should be handled by @Recover. Check Spring Retry proxy configuration.",
                    idempotencyKey, eventType, ex.getMessage(), ex);
            return false;
        } catch (Exception ex) {
            // 일반 실패 fallback 처리
            // 일반적으로 SystemMessageSender의 @Recover에서 처리되지만,
            // Spring Retry 프록시 미적용 또는 @Recover 내부 예외 전파 등의 엣지 케이스 대비 fallback
            // 선점 키를 삭제하여 재시도/재처리 가능 상태로 복구 (at-least-once 보장)
            processedEventStore.removeProcessed(idempotencyKey);
            LOG.error("[RetrySender] Unexpected exception from sendWithRetry (fallback - proxy inactive suspected). " +
                            "key={}, eventType={}. " +
                            "This should be handled by @Recover. Check Spring Retry proxy configuration.",
                    idempotencyKey, eventType, ex);
            return false;
        }
    }
}
