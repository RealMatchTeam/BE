package com.example.RealMatch.chat.application.event.sender;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.exception.ChatRoomNotFoundException;
import com.example.RealMatch.chat.application.exception.LogicalFailureException;
import com.example.RealMatch.chat.application.idempotency.ProcessedEventStore;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;

import io.micrometer.core.instrument.MeterRegistry;
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

    private static final String METRIC_LOGICAL_FAILURE = "chat.system_message.logical_failure";

    private final ProcessedEventStore processedEventStore;
    private final SystemMessageSender systemMessageSender;
    private final MeterRegistry meterRegistry;

    /**
     * IN_PROGRESS 상태의 TTL. 짧게 설정하여 프로세스 다운 시 빠르게 만료 → 재처리 가능.
     * 전송 + 재시도(3회, backoff 최대 800ms)를 충분히 커버하는 시간으로 설정.
     */
    private static final Duration IN_PROGRESS_TTL = Duration.ofMinutes(3);

    /**
     * PROCESSED 상태의 TTL. 길게 설정하여 동일 이벤트의 중복 처리를 장기간 방지.
     */
    private static final Duration PROCESSED_TTL = Duration.ofHours(6);

    /**
     * 시스템 메시지를 멱등성 체크 후 전송합니다.
     *
     * <p>2단계 상태머신:
     * <ol>
     *   <li>IN_PROGRESS(짧은 TTL)로 선점 → 프로세스 다운 시 빠르게 만료되어 재처리 가능</li>
     *   <li>전송 성공 시 PROCESSED(긴 TTL)로 승격 → 중복 처리 장기간 방지</li>
     * </ol>
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

        // 멱등성 체크: IN_PROGRESS(짧은 TTL)로 선점
        // false=중복만, throw=Redis 장애 등 판단 불가 → 상위로 throw하여 DLQ 처리
        boolean isNewEvent = processedEventStore.markIfNotProcessed(idempotencyKey, IN_PROGRESS_TTL);
        if (!isNewEvent) {
            LOG.info("[RetrySender] Event already processed or in progress, skipping. key={}, eventType={}",
                    idempotencyKey, eventType);
            return false;
        }

        try {
            // 실제 전송 (재시도 가능) - SystemMessageSender에 위임
            systemMessageSender.sendWithRetry(idempotencyKey, roomId, messageKind, payload, eventType, additionalData);
        } catch (LogicalFailureException ex) {
            // 논리적 실패: removeProcessed로 키 제거하고 false 반환 (DLQ 기록 안 함)
            processedEventStore.removeProcessed(idempotencyKey);
            recordLogicalFailure(eventType, resolveLogicalFailureReason(ex));
            LOG.warn("[RetrySender] Logical failure. key={}, eventType={}, error={}",
                    idempotencyKey, eventType, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage(), ex);
            return false;
        } catch (ChatRoomNotFoundException | IllegalArgumentException ex) {
            // fallback: @Recover가 동작하지 않은 경우 (proxy 비활성화 등)
            // 논리적 실패만 removeProcessed: 재시도해도 동일 결과이므로 키 제거 (레이스/중복 가능성 없음)
            processedEventStore.removeProcessed(idempotencyKey);
            recordLogicalFailure(eventType, ex.getClass().getSimpleName());
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

        // 전송 성공: IN_PROGRESS → PROCESSED로 승격 (긴 TTL)
        // 실패 시 throw → 상위에서 DLQ 처리 (성공처럼 넘기지 않음)
        processedEventStore.markAsProcessed(idempotencyKey, PROCESSED_TTL);
        LOG.info("[RetrySender] System message sent successfully. key={}, roomId={}, kind={}",
                idempotencyKey, roomId, messageKind);
        return true;
    }

    private void recordLogicalFailure(String eventType, String reason) {
        meterRegistry.counter(
                METRIC_LOGICAL_FAILURE,
                "eventType", eventType != null ? eventType : "unknown",
                "reason", reason != null ? reason : "unknown"
        ).increment();
    }

    private static String resolveLogicalFailureReason(LogicalFailureException ex) {
        if (ex.getCause() != null) {
            return ex.getCause().getClass().getSimpleName();
        }
        return "LogicalFailureException";
    }
}
