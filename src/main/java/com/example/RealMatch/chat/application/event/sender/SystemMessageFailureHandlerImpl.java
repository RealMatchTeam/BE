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
 * <p>재시도 소진 후 전송 실패 시 removeProcessed 호출 후 DLQ 기록.
 * 키를 제거해야 재처리(DLQ 재전송 등) 시 유실 없이 처리 가능합니다.
 * DLQ 기록 시도 후, 실패하면 예외를 전파하여 상위(@Recover)에서 DlqEnqueueFailedException로 래핑/ fallback 처리되도록 한다.
 */
@Component
@RequiredArgsConstructor
public class SystemMessageFailureHandlerImpl implements SystemMessageFailureHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SystemMessageFailureHandlerImpl.class);

    private static final String STAGE_SEND_AFTER_RETRIES = "send_after_retries";
    private static final String FAILURE_REASON_TRANSIENT_SEND_FAILED = "transient_send_failed";

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
        LOG.error("[FailureHandler] Handling system message failure (transient, after retries). " +
                        "key={}, roomId={}, kind={}, eventType={}",
                idempotencyKey, roomId, messageKind, eventType);

        // 전송 최종 실패: removeProcessed로 키 제거 → 재처리 시 유실 없음 (계약상 swallow)
        processedEventStore.removeProcessed(idempotencyKey);

        // DLQ 기록: stage=send_after_retries, failureReason으로 판단 가능하게
        // 실패 시 예외 전파 → @Recover에서 DlqEnqueueFailedException으로 래핑되어 fallback 처리되도록 한다.
        Map<String, Object> dlqData = new HashMap<>(additionalData != null ? additionalData : Map.of());
        dlqData.put("messageKind", messageKind != null ? messageKind.toString() : "UNKNOWN");
        dlqData.put("stage", STAGE_SEND_AFTER_RETRIES);
        dlqData.put("failureReason", FAILURE_REASON_TRANSIENT_SEND_FAILED);

        failedEventDlq.enqueueFailedEvent(
                eventType,
                idempotencyKey,
                roomId,
                errorMessage,
                dlqData
        );
    }
}
