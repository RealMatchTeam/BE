package com.example.RealMatch.chat.application.event.apply;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.exception.ChatRoomNotFoundException;
import com.example.RealMatch.chat.application.idempotency.FailedEventDlq;
import com.example.RealMatch.chat.application.idempotency.ProcessedEventStore;
import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryService;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatApplyCardPayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatApplyStatusNoticePayloadResponse;

import lombok.RequiredArgsConstructor;

/**
 * 지원(Apply) 관련 시스템 메시지 처리를 담당하는 핸들러
 */
@Component
@RequiredArgsConstructor
public class ApplySystemMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApplySystemMessageHandler.class);

    private final ChatMessageSocketService chatMessageSocketService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ProcessedEventStore processedEventStore;
    private final FailedEventDlq failedEventDlq;

    private static final Duration EVENT_IDEMPOTENCY_TTL = Duration.ofHours(6);

    /**
     * 지원 전송 이벤트 처리.
     * APPLY_CARD 시스템 메시지를 전송합니다.
     */
    @Async
    public void handleApplySent(ApplySentEvent event) {
        // 1. 논리적 검증 먼저 수행
        if (event.roomId() == null || event.payload() == null) {
            LOG.warn("[Apply] Invalid event. eventId={}, roomId={}, payload={}",
                    event.eventId(), event.roomId(), event.payload());
            return;
        }

        // 2. Redis 멱등성 체크 (중복 방지용, 전송 전에만 체크)
        if (!checkIfNotProcessed(event.eventId(), "ApplySentEvent")) {
            return;
        }

        try {
            LOG.info("[Apply] Processing apply sent. eventId={}, roomId={}, applyId={}, campaignId={}",
                    event.eventId(), event.roomId(), event.payload().applyId(), event.payload().campaignId());

            // 3. 전송 시도 (성공 시 Redis 키가 남음)
            sendApplyCard(event.roomId(), event.payload(), event.eventId());

            LOG.info("[Apply] System message sent. roomId={}, applyId={}, campaignId={}",
                    event.roomId(), event.payload().applyId(), event.payload().campaignId());

        } catch (IllegalArgumentException ex) {
            // 논리적 실패: Redis 키 삭제
            LOG.warn("[Apply] Logical failure. Removing Redis key. eventId={}, error={}",
                    event.eventId(), ex.getMessage());
            processedEventStore.removeProcessed(event.eventId());
        } catch (Exception ex) {
            LOG.error("[Apply] Failed to handle apply sent. eventId={}, roomId={}, applyId={}",
                    event.eventId(),
                    event.roomId(),
                    event.payload() != null ? event.payload().applyId() : null,
                    ex);
            // 전송 실패 시 Redis 키는 유지됨 (재시도 방지)
            // 최종 실패 시 @Recover에서 키를 삭제하여 재처리 가능하도록 함
        }
    }

    /**
     * 지원 상태 변경 이벤트 처리.
     * APPLY_STATUS_NOTICE 시스템 메시지를 전송합니다.
     */
    @Async
    public void handleApplyStatusChanged(ApplyStatusChangedEvent event) {
        // 1. 채팅방 조회 (논리적 검증을 먼저 수행)
        Optional<Long> roomIdOpt = chatRoomQueryService.getRoomIdByUserPair(
                event.brandUserId(), event.creatorUserId());
        if (roomIdOpt.isEmpty()) {
            LOG.warn("[Apply] Chat room not found. eventId={}, applyId={}, brandUserId={}, creatorUserId={}",
                    event.eventId(), event.applyId(), event.brandUserId(), event.creatorUserId());
            // 논리적 실패는 Redis 키를 남기지 않음
            return;
        }
        Long roomId = roomIdOpt.get();

        // 2. Redis 멱등성 체크 (중복 방지용, 전송 전에만 체크)
        if (!checkIfNotProcessed(event.eventId(), "ApplyStatusChangedEvent")) {
            return;
        }

        try {
            LOG.info("[Apply] Processing status change. eventId={}, applyId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                    event.eventId(), event.applyId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

            // 3. 상태 변경 알림 메시지 전송 (재시도 가능, 성공 시 Redis 키 유지)
            sendApplyStatusNotice(roomId, event.applyId(), event.actorUserId(), event.eventId());

            LOG.info("[Apply] Status change processed. roomId={}, applyId={}, status={}",
                    roomId, event.applyId(), event.newStatus());

        } catch (ChatRoomNotFoundException ex) {
            // 논리적 실패: Redis 키 삭제
            LOG.warn("[Apply] Logical failure (room not found). Removing Redis key. eventId={}",
                    event.eventId());
            processedEventStore.removeProcessed(event.eventId());
        } catch (Exception ex) {
            LOG.error("[Apply] Failed to handle status change. eventId={}, applyId={}, brandUserId={}, creatorUserId={}",
                    event.eventId(),
                    event.applyId(),
                    event.brandUserId(),
                    event.creatorUserId(),
                    ex);
            // 전송 실패 시 Redis 키는 유지됨 (재시도 방지)
            // 최종 실패는 @Recover에서 처리
        }
    }

    /**
     * 지원 카드 전송 (재시도 가능)
     * 전송 성공 시 Redis 키를 확실히 남깁니다.
     */
    @Retryable(
            retryFor = {Exception.class},
            noRetryFor = {IllegalArgumentException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 800)
    )
    private void sendApplyCard(Long roomId, ChatApplyCardPayloadResponse payload, String eventId) {
        try {
            LOG.info("[Apply] Attempting to send apply card. eventId={}, roomId={}, applyId={}",
                    eventId, roomId, payload != null ? payload.applyId() : null);
            chatMessageSocketService.sendSystemMessage(roomId, ChatSystemMessageKind.APPLY_CARD, payload);
            // 전송 성공 후 Redis 키를 확실히 남김
            processedEventStore.markAsProcessed(eventId, EVENT_IDEMPOTENCY_TTL);
            LOG.info("[Apply] Apply card sent successfully. eventId={}, roomId={}", eventId, roomId);
        } catch (Exception ex) {
            // 재시도 가능한 예외는 Spring Retry가 처리
            // 최종 실패는 @Recover에서 키 삭제
            LOG.warn("[Apply] Failed to send apply card (will retry). eventId={}, roomId={}, error={}",
                    eventId, roomId, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 지원 상태 변경 알림 메시지 전송 (재시도 가능)
     * 전송 성공 시 Redis 키를 확실히 남깁니다.
     */
    @Retryable(
            retryFor = {Exception.class},
            noRetryFor = {ChatRoomNotFoundException.class, IllegalArgumentException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 800)
    )
    private void sendApplyStatusNotice(Long roomId, Long applyId, Long actorUserId, String eventId) {
        try {
            LOG.info("[Apply] Attempting to send apply status notice. eventId={}, roomId={}, applyId={}",
                    eventId, roomId, applyId);
            ChatApplyStatusNoticePayloadResponse statusNoticePayload =
                    new ChatApplyStatusNoticePayloadResponse(
                            applyId,
                            actorUserId,
                            LocalDateTime.now()
                    );
            chatMessageSocketService.sendSystemMessage(
                    roomId,
                    ChatSystemMessageKind.APPLY_STATUS_NOTICE,
                    statusNoticePayload
            );
            // 전송 성공 후 Redis 키를 확실히 남김
            processedEventStore.markAsProcessed(eventId, EVENT_IDEMPOTENCY_TTL);
            LOG.info("[Apply] Apply status notice sent successfully. eventId={}, roomId={}", eventId, roomId);
        } catch (Exception ex) {
            // 재시도 가능한 예외는 Spring Retry가 처리
            // 최종 실패는 @Recover에서 키 삭제
            LOG.warn("[Apply] Failed to send apply status notice (will retry). eventId={}, roomId={}, error={}",
                    eventId, roomId, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 지원 카드 전송 실패 시 복구 처리
     */
    @Recover
    private void recoverApplyCard(Exception ex, Long roomId, ChatApplyCardPayloadResponse payload, String eventId) {
        // 전송 실패 시 Redis 키 삭제 (재시도 가능하도록)
        processedEventStore.removeProcessed(eventId);
        LOG.error("[Apply] Failed to send apply card after all retries (3 attempts). " +
                        "eventId={}, roomId={}, applyId={}, Redis key removed",
                eventId, roomId, payload != null ? payload.applyId() : null, ex);
        
        Map<String, Object> additionalData = new HashMap<>();
        if (payload != null && payload.applyId() != null) {
            additionalData.put("applyId", payload.applyId());
        }
        failedEventDlq.enqueueFailedEvent(
                "ApplySentEvent",
                eventId,
                roomId,
                ex.getMessage(),
                additionalData
        );
    }

    /**
     * 지원 상태 변경 알림 메시지 전송 실패 시 복구 처리
     */
    @Recover
    private void recoverApplyStatusNotice(Exception ex, Long roomId, Long applyId, Long actorUserId, String eventId) {
        // 전송 실패 시 Redis 키 삭제 (재시도 가능하도록)
        processedEventStore.removeProcessed(eventId);
        LOG.error("[Apply] Failed to send apply status notice after all retries (3 attempts). " +
                        "eventId={}, roomId={}, applyId={}, actorUserId={}, Redis key removed",
                eventId, roomId, applyId, actorUserId, ex);
        
        Map<String, Object> additionalData = new HashMap<>();
        if (applyId != null) {
            additionalData.put("applyId", applyId);
        }
        if (actorUserId != null) {
            additionalData.put("actorUserId", actorUserId);
        }
        failedEventDlq.enqueueFailedEvent(
                "ApplyStatusChangedEvent",
                eventId,
                roomId,
                ex.getMessage(),
                additionalData
        );
    }

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
    private boolean checkIfNotProcessed(String eventId, String eventType) {
        if (eventId == null) {
            LOG.error("[Apply] {} has null eventId, this should never happen. Rejecting event.", eventType);
            throw new IllegalStateException("Event ID cannot be null for " + eventType);
        }

        // SETNX로 처리 시작 시점에 키 생성 (중복 체크)
        boolean isNewEvent = processedEventStore.markIfNotProcessed(eventId, EVENT_IDEMPOTENCY_TTL);
        if (!isNewEvent) {
            LOG.warn("[Apply] Duplicate {} detected, skipping. eventId={}", eventType, eventId);
            return false;
        }

        return true;
    }
}
