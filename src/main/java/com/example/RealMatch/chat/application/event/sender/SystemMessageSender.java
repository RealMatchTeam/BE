package com.example.RealMatch.chat.application.event.sender;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.exception.ChatRoomNotFoundException;
import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;

import lombok.RequiredArgsConstructor;

/**
 * 시스템 메시지 전송 및 재시도 로직을 담당하는 컴포넌트.
 *
 * <p>실제 메시지 전송과 재시도 로직만을 담당하며,
 * 멱등성 체크, 상태 관리, 실패 처리 정책은 SystemMessageRetrySender와 SystemMessageFailureHandler에서 처리합니다.
 */
@Component
@RequiredArgsConstructor
public class SystemMessageSender {

    private static final Logger LOG = LoggerFactory.getLogger(SystemMessageSender.class);

    private final ChatMessageSocketService chatMessageSocketService;
    private final SystemMessageFailureHandler failureHandler;

    /**
     * 시스템 메시지를 전송합니다.
     *
     * <p>@Retryable이 적용되어 예외 발생 시 재시도됩니다.
     * 전송 성공 시에는 예외를 던지지 않으며, 실패 시에만 예외가 전파됩니다.
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
        LOG.info("[MessageSender] Attempting to send system message. key={}, roomId={}, kind={}, eventType={}",
                idempotencyKey, roomId, messageKind, eventType);

        chatMessageSocketService.sendSystemMessage(roomId, messageKind, payload);

        LOG.info("[MessageSender] System message sent successfully. key={}, roomId={}, kind={}",
                idempotencyKey, roomId, messageKind);
    }

    /**
     * 논리적 실패(ChatRoomNotFoundException) 처리.
     *
     * <p>논리적 실패는 재시도하지 않으며, DLQ에도 기록하지 않습니다.
     * RetrySender에서 이미 키 삭제를 처리하므로 여기서는 로그만 남깁니다.
     */
    @Recover
    public void recoverLogicalFailure(
            ChatRoomNotFoundException ex,
            String idempotencyKey,
            Long roomId,
            ChatSystemMessageKind messageKind,
            ChatSystemMessagePayload payload,
            String eventType,
            Map<String, Object> additionalData
    ) {
        LOG.warn("[MessageSender] Logical failure (ChatRoomNotFound). Not retrying, not DLQ. " +
                        "key={}, roomId={}, kind={}, eventType={}",
                idempotencyKey, roomId, messageKind, eventType);
        // 논리적 실패는 DLQ에 기록하지 않음 (정책)
    }

    /**
     * 논리적 실패(IllegalArgumentException) 처리.
     *
     * <p>논리적 실패는 재시도하지 않으며, DLQ에도 기록하지 않습니다.
     * RetrySender에서 이미 키 삭제를 처리하므로 여기서는 로그만 남깁니다.
     */
    @Recover
    public void recoverLogicalFailure(
            IllegalArgumentException ex,
            String idempotencyKey,
            Long roomId,
            ChatSystemMessageKind messageKind,
            ChatSystemMessagePayload payload,
            String eventType,
            Map<String, Object> additionalData
    ) {
        LOG.warn("[MessageSender] Logical failure (IllegalArgument). Not retrying, not DLQ. " +
                        "key={}, roomId={}, kind={}, eventType={}",
                idempotencyKey, roomId, messageKind, eventType);
        // 논리적 실패는 DLQ에 기록하지 않음 (정책)
    }

    /**
     * 재시도 후에도 전송에 실패한 경우 호출되어 실패 처리자에게 위임합니다.
     *
     * <p>@Recover는 절대 예외를 던지지 않도록 보장합니다.
     * failureHandler 내부 예외도 catch하여 전체 흐름을 보호합니다.
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
        LOG.error("[MessageSender] Failed to send system message after all retries (3 attempts). " +
                        "key={}, roomId={}, kind={}, eventType={}",
                idempotencyKey, roomId, messageKind, eventType, ex);

        // 실패 처리 정책을 failureHandler에 위임
        // failureHandler 내부 예외도 catch하여 @Recover가 절대 예외를 던지지 않도록 보장
        try {
            failureHandler.handleFailure(
                    idempotencyKey,
                    roomId,
                    messageKind,
                    eventType,
                    ex.getMessage(),
                    additionalData
            );
        } catch (Exception handlerEx) {
            LOG.error("[MessageSender] FailureHandler threw exception. key={}, eventType={}",
                    idempotencyKey, eventType, handlerEx);
            // 예외를 다시 던지지 않음 (@Recover는 절대 throw 안 함)
        }
    }
}
