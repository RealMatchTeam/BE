package com.example.RealMatch.chat.application.event.apply;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.event.sender.SystemMessageRetrySender;
import com.example.RealMatch.chat.application.exception.ChatRoomNotFoundException;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryService;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatApplyStatusNoticePayloadResponse;

import lombok.RequiredArgsConstructor;

/**
 * 지원(Apply) 관련 시스템 메시지 처리를 담당하는 핸들러.
 * 
 * <p>역할: 오케스트레이션 (payload 준비, roomId 찾기, 메시지 종류 결정, 멱등성 키 결정)
 * <p>실제 전송/재시도/멱등성/DLQ는 SystemMessageRetrySender가 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class ApplySystemMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApplySystemMessageHandler.class);

    private final SystemMessageRetrySender retrySender;
    private final ChatRoomQueryService chatRoomQueryService;

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

        LOG.info("[Apply] Processing apply sent. eventId={}, roomId={}, applyId={}, campaignId={}",
                event.eventId(), event.roomId(), event.payload().applyId(), event.payload().campaignId());

        // 2. 멱등성 키 결정 (deterministic)
        String idempotencyKey = event.eventId(); // 이미 deterministic: "APPLY_SENT:{applyId}"

        // 3. RetrySender로 전송 (멱등성 체크 + 재시도 + DLQ 포함)
        Map<String, Object> additionalData = new HashMap<>();
        if (event.payload().applyId() != null) {
            additionalData.put("applyId", event.payload().applyId());
        }
        if (event.payload().campaignId() != null) {
            additionalData.put("campaignId", event.payload().campaignId());
        }
        additionalData.put("messageKind", ChatSystemMessageKind.APPLY_CARD.toString());

        try {
            boolean sent = retrySender.sendWithIdempotency(
                    idempotencyKey,
                    event.roomId(),
                    ChatSystemMessageKind.APPLY_CARD,
                    event.payload(),
                    "ApplySentEvent",
                    additionalData
            );

            if (sent) {
                LOG.info("[Apply] System message sent. roomId={}, applyId={}, campaignId={}",
                        event.roomId(), event.payload().applyId(), event.payload().campaignId());
            } else {
                LOG.warn("[Apply] Duplicate event, skipped. eventId={}", event.eventId());
            }
        } catch (IllegalArgumentException ex) {
            // 논리적 실패는 RetrySender에서 이미 처리됨
            LOG.warn("[Apply] Logical failure. eventId={}, error={}", event.eventId(), ex.getMessage());
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

        try {
            LOG.info("[Apply] Processing status change. eventId={}, applyId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                    event.eventId(), event.applyId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

            // 2. 상태 변경 알림 메시지 전송 (별도 멱등성 키)
            String statusNoticeKey = String.format("%s:NOTICE", event.eventId());
            ChatApplyStatusNoticePayloadResponse statusNoticePayload =
                    new ChatApplyStatusNoticePayloadResponse(
                            event.applyId(),
                            event.actorUserId(),
                            LocalDateTime.now()
                    );

            Map<String, Object> noticeData = new HashMap<>();
            noticeData.put("applyId", event.applyId());
            if (event.actorUserId() != null) {
                noticeData.put("actorUserId", event.actorUserId());
            }

            retrySender.sendWithIdempotency(
                    statusNoticeKey,
                    roomId,
                    ChatSystemMessageKind.APPLY_STATUS_NOTICE,
                    statusNoticePayload,
                    "ApplyStatusChangedEvent",
                    noticeData
            );

            LOG.info("[Apply] Status change processed. roomId={}, applyId={}, status={}",
                    roomId, event.applyId(), event.newStatus());

        } catch (ChatRoomNotFoundException ex) {
            // 논리적 실패는 RetrySender에서 이미 처리됨
            LOG.warn("[Apply] Logical failure (room not found). eventId={}", event.eventId());
        } catch (Exception ex) {
            LOG.error("[Apply] Failed to handle status change. eventId={}, applyId={}, brandUserId={}, creatorUserId={}",
                    event.eventId(),
                    event.applyId(),
                    event.brandUserId(),
                    event.creatorUserId(),
                    ex);
            // 전송 실패는 RetrySender의 @Recover에서 처리됨
        }
    }
}
