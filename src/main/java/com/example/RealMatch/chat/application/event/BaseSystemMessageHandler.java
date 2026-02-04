package com.example.RealMatch.chat.application.event;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.example.RealMatch.chat.application.event.sender.SystemMessageRetrySender;
import com.example.RealMatch.chat.application.idempotency.FailedEventDlq;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;

import lombok.RequiredArgsConstructor;

/**
 * 시스템 메시지 이벤트 처리의 공통 실행 템플릿.
 *
 * <p>payload 생성 및 전송 단계에서 발생하는 예외를 일관되게 처리하고,
 * 실패 이벤트를 DLQ에 기록하여 추적성을 보장합니다.
 * 실제 메시지 전송과 멱등성 판단은 외부 컴포넌트에 위임합니다.
 */
@RequiredArgsConstructor
public abstract class BaseSystemMessageHandler {

    protected final FailedEventDlq failedEventDlq;
    protected final SystemMessageRetrySender retrySender;

    protected abstract Logger getLogger();

    /**
     * 표준 실행 템플릿: payload 생성 및 전송을 한 번에 처리합니다.
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>payload 생성 (supplier) 실행 - 예외 발생 시 DLQ enqueue 후 종료</li>
     *   <li>sendAction 실행 - 예외 발생 시 DLQ enqueue</li>
     * </ol>
     *
     * @param meta 이벤트 메타데이터 (idempotencyKey, roomId, eventType)
     * @param contextData DLQ에 저장할 컨텍스트 데이터 (applyId, proposalId, campaignId, messageKind 등)
     *                    null이어도 안전하게 처리되며, 실패 시 DLQ에 포함되어 추적성을 보장합니다
     * @param payloadSupplier payload를 생성하는 supplier (예외 발생 시 DLQ enqueue)
     * @param sendAction payload를 받아서 전송하는 action (내부적으로 retrySender 호출, 예외 발생 시 DLQ enqueue)
     */
    protected void execute(
            SystemEventMeta meta,
            Map<String, Object> contextData,
            Supplier<ChatSystemMessagePayload> payloadSupplier,
            Consumer<ChatSystemMessagePayload> sendAction
    ) {
        Map<String, Object> safeContext = (contextData != null) ? contextData : Map.of();

        // 1) payload 생성 시도
        final ChatSystemMessagePayload payload;
        try {
            payload = payloadSupplier.get();
        } catch (Exception ex) {
            getLogger().error("[{}] Failed to create payload. idempotencyKey={}, roomId={}, error={}",
                    meta.eventType(), meta.idempotencyKey(), meta.roomId(), ex.getMessage(), ex);

            Map<String, Object> dlqData = new HashMap<>(safeContext);
            dlqData.put("failureReason", "payload_creation_failed");
            dlqData.put("stage", "payload_supplier");
            dlqData.put("handler", this.getClass().getSimpleName());

            failedEventDlq.enqueueFailedEvent(
                    meta.eventType(),
                    meta.idempotencyKey(),
                    meta.roomId(),
                    ex.getMessage(),
                    dlqData
            );
            return;
        }

        // 2) 전송 실행 (sendAction 내부 예외도 DLQ로 처리)
        try {
            sendAction.accept(payload);
        } catch (Exception ex) {
            getLogger().error("[{}] Failed to execute sendAction. idempotencyKey={}, roomId={}, error={}",
                    meta.eventType(), meta.idempotencyKey(), meta.roomId(), ex.getMessage(), ex);

            Map<String, Object> dlqData = new HashMap<>(safeContext);
            dlqData.put("failureReason", "send_action_failed");
            dlqData.put("stage", "send_action");
            dlqData.put("handler", this.getClass().getSimpleName());

            failedEventDlq.enqueueFailedEvent(
                    meta.eventType(),
                    meta.idempotencyKey(),
                    meta.roomId(),
                    ex.getMessage(),
                    dlqData
            );
        }
    }
}
