package com.example.RealMatch.chat.application.event.sender;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.exception.ChatRoomNotFoundException;
import com.example.RealMatch.chat.application.idempotency.FailedEventDlq;
import com.example.RealMatch.chat.application.idempotency.ProcessedEventStore;
import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;

import lombok.RequiredArgsConstructor;

/**
 * 시스템 메시지 전송의 재시도, 멱등성, DLQ 처리를 담당하는 전용 컴포넌트.
 * 
 * <self-invocation 문제를 해결하기 위해 별도 Bean으로 분리되었습니다.
 * 
 * <p>처리 흐름:
 * <ol>
 *   <li>멱등성 체크: markIfNotProcessed(idempotencyKey, TTL)</li>
 *   <li>메시지 전송: chatMessageSocketService.sendSystemMessage(...)</li>
 *   <li>성공 시: markAsProcessed(idempotencyKey, TTL)</li>
 *   <li>실패 시: @Retryable로 최대 3회 재시도</li>
 *   <li>최종 실패 시: @Recover에서 removeProcessed + DLQ enqueue</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class SystemMessageRetrySender {

    private static final Logger LOG = LoggerFactory.getLogger(SystemMessageRetrySender.class);

    private final ChatMessageSocketService chatMessageSocketService;
    private final ProcessedEventStore processedEventStore;
    private final FailedEventDlq failedEventDlq;
    
    @Autowired
    private ApplicationContext applicationContext;

    private static final Duration EVENT_IDEMPOTENCY_TTL = Duration.ofHours(6);

    private SystemMessageRetrySender getSelf() {
        return applicationContext.getBean(SystemMessageRetrySender.class);
    }

    /**
     * 시스템 메시지를 멱등성 체크와 함께 전송합니다.
     * 
     * <p>처리 흐름:
     * <ol>
     *   <li>멱등성 체크: markIfNotProcessed() - 이미 처리된 이벤트면 skip</li>
     *   <li>실제 전송: sendWithRetry() - @Retryable로 최대 3회 재시도</li>
     *   <li>성공 시: markAsProcessed()로 키 확정</li>
     *   <li>실패 시: @Recover에서 removeProcessed() + DLQ enqueue</li>
     * </ol>
     * 
     * @param idempotencyKey 멱등성 키 (deterministic, 예: "PROPOSAL_SENT:123")
     * @param roomId 채팅방 ID
     * @param messageKind 시스템 메시지 종류
     * @param payload 페이로드
     * @param eventType 이벤트 타입 (로깅 및 DLQ용, 예: "ProposalSentEvent")
     * @param additionalData DLQ에 저장할 추가 데이터
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
            LOG.warn("[RetrySender] Duplicate event detected, skipping. key={}, eventType={}",
                    idempotencyKey, eventType);
            return false;
        }

        try {
            // 실제 전송 (재시도 가능)
            // self-invocation 방지를 위해 프록시를 통해 호출
            getSelf().sendWithRetry(idempotencyKey, roomId, messageKind, payload, eventType, additionalData);
            return true;
        } catch (ChatRoomNotFoundException | IllegalArgumentException ex) {
            // 논리적 실패: Redis 키 삭제 (재시도 안 함)
            processedEventStore.removeProcessed(idempotencyKey);
            LOG.warn("[RetrySender] Logical failure. Removing Redis key. key={}, eventType={}, error={}",
                    idempotencyKey, eventType, ex.getMessage());
            return false;
        } catch (Exception ex) {
            // 전송 실패는 @Recover에서 처리됨 (키 삭제 + DLQ)
            // 여기서는 로그만 남기고 false 반환
            LOG.error("[RetrySender] Failed to send after all retries. key={}, eventType={}",
                    idempotencyKey, eventType, ex);
            return false;
        }
    }

    /**
     * 시스템 메시지 전송 (재시도 가능)
     * 
     * <p>이 메서드는 public이므로 프록시를 통해 호출되어 @Retryable이 정상 동작합니다.
     * getSelf()를 통해 자기 자신의 프록시를 가져와 호출하므로 self-invocation 문제가 없습니다.
     */
    @Retryable(
            retryFor = {Exception.class},
            noRetryFor = {ChatRoomNotFoundException.class, IllegalArgumentException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 800)
    )
    public void sendWithRetry(
            String idempotencyKey,
            Long roomId,
            ChatSystemMessageKind messageKind,
            ChatSystemMessagePayload payload,
            String eventType,
            Map<String, Object> additionalData
    ) {
        LOG.info("[RetrySender] Attempting to send system message. key={}, roomId={}, kind={}, eventType={}",
                idempotencyKey, roomId, messageKind, eventType);

        chatMessageSocketService.sendSystemMessage(roomId, messageKind, payload);

        // 전송 성공 후 Redis 키를 확실히 남김
        processedEventStore.markAsProcessed(idempotencyKey, EVENT_IDEMPOTENCY_TTL);
        LOG.info("[RetrySender] System message sent successfully. key={}, roomId={}, kind={}",
                idempotencyKey, roomId, messageKind);
    }

    /**
     * 시스템 메시지 전송 최종 실패 시 복구 처리
     */
    @Recover
    public void recoverSystemMessage(
            Exception ex,
            String idempotencyKey,
            Long roomId,
            ChatSystemMessageKind messageKind,
            ChatSystemMessagePayload payload,
            String eventType,
            Map<String, Object> additionalData
    ) {
        // 전송 실패 시 Redis 키 삭제 (재시도 가능하도록)
        processedEventStore.removeProcessed(idempotencyKey);
        LOG.error("[RetrySender] Failed to send system message after all retries (3 attempts). " +
                        "key={}, roomId={}, kind={}, eventType={}, Redis key removed",
                idempotencyKey, roomId, messageKind, eventType, ex);

        Map<String, Object> dlqData = new HashMap<>(additionalData != null ? additionalData : Map.of());
        dlqData.put("messageKind", messageKind != null ? messageKind.toString() : "UNKNOWN");
        failedEventDlq.enqueueFailedEvent(
                eventType,
                idempotencyKey,
                roomId,
                ex.getMessage(),
                dlqData
        );
    }
}
