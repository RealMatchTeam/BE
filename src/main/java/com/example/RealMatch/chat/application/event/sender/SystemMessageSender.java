package com.example.RealMatch.chat.application.event.sender;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.exception.ChatRoomNotFoundException;
import com.example.RealMatch.chat.application.exception.DlqEnqueueFailedException;
import com.example.RealMatch.chat.application.exception.DlqEnqueuedException;
import com.example.RealMatch.chat.application.exception.LogicalFailureException;
import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;

import lombok.RequiredArgsConstructor;

/**
 * 시스템 메시지 전송 및 재시도 로직을 담당하는 컴포넌트.
 *
 * <p>전송·재시도와, 재시도 소진 후 실패 시 FailureHandler 위임까지 담당합니다.
 * 멱등성 체크 및 전송 성공 시 상태 관리는 SystemMessageRetrySender,
 * 재시도 소진 시 구체적인 정책(키 삭제, DLQ 기록)은 SystemMessageFailureHandler 구현체에서 수행합니다.
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
            retryFor = {RuntimeException.class},
            noRetryFor = {
                    ChatRoomNotFoundException.class,
                    IllegalArgumentException.class,
                    LogicalFailureException.class,
                    DlqEnqueuedException.class,
                    DlqEnqueueFailedException.class
            },
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
     * LogicalFailureException을 던져서 RetrySender가 removeProcessed()를 호출하고 false를 반환하도록 합니다.
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
        // 예외를 다시 던져서 RetrySender가 removeProcessed()를 호출하고 false를 반환하도록 함
        throw new LogicalFailureException(
                String.format("Logical failure: ChatRoomNotFound. key=%s, roomId=%s", idempotencyKey, roomId),
                ex
        );
    }

    /**
     * 논리적 실패(IllegalArgumentException) 처리.
     *
     * <p>논리적 실패는 재시도하지 않으며, DLQ에도 기록하지 않습니다.
     * LogicalFailureException을 던져서 RetrySender가 removeProcessed()를 호출하고 false를 반환하도록 합니다.
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
        // 예외를 다시 던져서 RetrySender가 removeProcessed()를 호출하고 false를 반환하도록 함
        throw new LogicalFailureException(
                String.format("Logical failure: IllegalArgument. key=%s, roomId=%s", idempotencyKey, roomId),
                ex
        );
    }

    /**
     * 재시도 소진 후 전송 실패 시: removeProcessed + DLQ 기록 후 DlqEnqueuedException throw.
     * DLQ enqueue 실패 시에는 DlqEnqueuedException을 던지지 않고 상위로 전파하여
     * BaseSystemMessageHandler가 fallback DLQ enqueue 하도록 합니다.
     */
    @Recover
    public void recoverSystemMessage(
            RuntimeException ex,
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

        try {
            failureHandler.handleFailure(
                    idempotencyKey,
                    roomId,
                    messageKind,
                    eventType,
                    ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName(),
                    additionalData
            );
        } catch (Exception handlerEx) {
            LOG.error("[MessageSender] FailureHandler (DLQ enqueue) failed. key={}, eventType={}. Fallback to BaseHandler.",
                    idempotencyKey, eventType, handlerEx);
            throw new DlqEnqueueFailedException(
                    "DLQ enqueue failed for key=" + idempotencyKey,
                    ex,        // cause = 원래 전송 실패
                    handlerEx  // suppressed = DLQ enqueue 실패
            );
        }
        throw new DlqEnqueuedException("DLQ enqueued for idempotencyKey=" + idempotencyKey, ex);
    }
}
