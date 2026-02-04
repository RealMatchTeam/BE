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
 * 시스템 메시지 전송을 위한 재시도, 멱등성, DLQ 처리를 담당하는 컴포넌트.
 *
 * <p>중복 이벤트를 방지하기 위해 멱등성 키를 관리하며,
 * 전송 실패 시 재시도 후 최종 실패는 DLQ로 위임합니다.
 *
 * <p>sendWithIdempotency()의 반환값은 실제 전송 성공 여부가 아니라,
 * 중복이 아니어서 전송 파이프라인에 진입했는지(accepted)를 의미합니다.
 * 전송 성공/실패 여부는 로그 및 DLQ를 통해 관측합니다.
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
     * 시스템 메시지를 멱등성 체크 후 전송 파이프라인에 진입시킵니다.
     *
     * <p>반환값은 실제 전송 성공 여부가 아니라,
     * 중복이 아니어서 전송 시도(+retry)가 수행되었는지(accepted)를 의미합니다.
     *
     * @return accepted 여부
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
            // 실제 전송 (재시도 가능) - self-invocation 방지를 위해 프록시를 통해 호출
            getSelf().sendWithRetry(idempotencyKey, roomId, messageKind, payload, eventType, additionalData);
            return true; // accepted
        } catch (ChatRoomNotFoundException | IllegalArgumentException ex) {
            // 논리적 실패: Redis 키 삭제 (재시도 안 함)
            processedEventStore.removeProcessed(idempotencyKey);
            LOG.warn("[RetrySender] Logical failure. Not retrying. key={}, eventType={}, error={}",
                    idempotencyKey, eventType, ex.getMessage());
            return false;
        } catch (Exception ex) {
            // 일반적으로 @Recover가 처리하지만, 혹시라도 예외가 전파되는 경우 방어적으로 false.
            LOG.error("[RetrySender] Unexpected exception from sendWithRetry. key={}, eventType={}",
                    idempotencyKey, eventType, ex);
            return false;
        }
    }

    /**
     * 시스템 메시지를 전송합니다.
     *
     * <p>@Retryable이 적용되어 예외 발생 시 재시도됩니다.
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
     * 재시도 후에도 전송에 실패한 경우 호출되어 DLQ로 위임합니다.
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
