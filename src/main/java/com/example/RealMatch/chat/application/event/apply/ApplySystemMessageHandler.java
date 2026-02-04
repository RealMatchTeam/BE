package com.example.RealMatch.chat.application.event.apply;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryService;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
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

    // 이벤트 중복 처리를 위한 간단한 인메모리 캐시 (프로덕션에서는 Redis 권장)
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();
    private static final int MAX_PROCESSED_EVENTS = 10000;

    /**
     * 지원 전송 이벤트 처리.
     * APPLY_CARD 시스템 메시지를 전송합니다.
     */
    @Async
    public void handleApplySent(ApplySentEvent event) {
        if (!validateAndMarkProcessed(event.eventId(), "ApplySentEvent")) {
            return;
        }

        try {
            if (event.roomId() == null || event.payload() == null) {
                LOG.warn("[Apply] Invalid event. roomId={}, payload={}",
                        event.roomId(), event.payload());
                return;
            }

            LOG.info("[Apply] Processing apply sent. eventId={}, roomId={}, applyId={}, campaignId={}",
                    event.eventId(), event.roomId(), event.payload().applyId(), event.payload().campaignId());

            chatMessageSocketService.sendSystemMessage(
                    event.roomId(),
                    ChatSystemMessageKind.APPLY_CARD,
                    event.payload()
            );

            LOG.info("[Apply] System message sent. roomId={}, applyId={}, campaignId={}",
                    event.roomId(), event.payload().applyId(), event.payload().campaignId());

        } catch (Exception ex) {
            LOG.error("[Apply] Failed to handle apply sent. eventId={}, roomId={}, applyId={}",
                    event.eventId(),
                    event.roomId(),
                    event.payload() != null ? event.payload().applyId() : null,
                    ex);
        }
    }

    /**
     * 지원 상태 변경 이벤트 처리.
     * APPLY_STATUS_NOTICE 시스템 메시지를 전송합니다.
     */
    @Async
    public void handleApplyStatusChanged(ApplyStatusChangedEvent event) {
        if (!validateAndMarkProcessed(event.eventId(), "ApplyStatusChangedEvent")) {
            return;
        }

        try {
            LOG.info("[Apply] Processing status change. eventId={}, applyId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                    event.eventId(), event.applyId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

            // 1. 채팅방 조회
            Optional<Long> roomIdOpt = chatRoomQueryService.getRoomIdByUserPair(
                    event.brandUserId(), event.creatorUserId());
            if (roomIdOpt.isEmpty()) {
                LOG.debug("[Apply] Chat room not found. brandUserId={}, creatorUserId={}",
                        event.brandUserId(), event.creatorUserId());
                return;
            }
            Long roomId = roomIdOpt.get();

            // 2. 상태 변경 알림 메시지 전송
            ChatApplyStatusNoticePayloadResponse statusNoticePayload =
                    new ChatApplyStatusNoticePayloadResponse(
                            event.applyId(),
                            event.actorUserId(),
                            LocalDateTime.now()
                    );
            chatMessageSocketService.sendSystemMessage(
                    roomId,
                    ChatSystemMessageKind.APPLY_STATUS_NOTICE,
                    statusNoticePayload
            );

            LOG.info("[Apply] Status change processed. roomId={}, applyId={}, status={}",
                    roomId, event.applyId(), event.newStatus());

        } catch (Exception ex) {
            LOG.error("[Apply] Failed to handle status change. eventId={}, applyId={}, brandUserId={}, creatorUserId={}",
                    event.eventId(),
                    event.applyId(),
                    event.brandUserId(),
                    event.creatorUserId(),
                    ex);
        }
    }

    /**
     * 이벤트 중복 처리 검증 및 마킹.
     * @return true면 처리 진행, false면 중복이므로 스킵
     */
    private boolean validateAndMarkProcessed(String eventId, String eventType) {
        if (eventId == null) {
            LOG.warn("[Apply] {} has null eventId, skipping duplicate check", eventType);
            return true;
        }

        // 캐시 크기 제한 (간단한 구현, 프로덕션에서는 TTL 기반 Redis 권장)
        if (processedEventIds.size() > MAX_PROCESSED_EVENTS) {
            processedEventIds.clear();
            LOG.info("[Apply] Cleared processed event cache (size exceeded)");
        }

        if (!processedEventIds.add(eventId)) {
            LOG.warn("[Apply] Duplicate {} detected, skipping. eventId={}", eventType, eventId);
            return false;
        }

        return true;
    }
}
