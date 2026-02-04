package com.example.RealMatch.chat.application.event.apply;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.event.BaseSystemMessageHandler;
import com.example.RealMatch.chat.application.event.SystemEventMeta;
import com.example.RealMatch.chat.application.event.sender.SystemMessageRetrySender;
import com.example.RealMatch.chat.application.idempotency.FailedEventDlq;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryService;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatApplyStatusNoticePayloadResponse;

/**
 * 지원(Apply) 시스템 메시지 이벤트 핸들러.
 *
 * <p>이 클래스는 이벤트 오케스트레이션만 담당하며,
 * 메시지 전송, 재시도, 멱등성, DLQ 처리는 공통 컴포넌트에 위임합니다.
 * sendWithIdempotency의 반환값은 전송 성공이 아닌 이벤트 수락(accepted) 여부를 의미합니다.
 */
@Component
public class ApplySystemMessageHandler extends BaseSystemMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApplySystemMessageHandler.class);

    private final ChatRoomQueryService chatRoomQueryService;

    public ApplySystemMessageHandler(
            FailedEventDlq failedEventDlq,
            SystemMessageRetrySender retrySender,
            ChatRoomQueryService chatRoomQueryService
    ) {
        super(failedEventDlq, retrySender);
        this.chatRoomQueryService = chatRoomQueryService;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * 지원 전송 이벤트 처리.
     * APPLY_CARD 시스템 메시지를 전송합니다.
     */
    @Async
    public void handleApplySent(ApplySentEvent event) {
        // 논리적 검증
        if (event.roomId() == null || event.payload() == null) {
            LOG.warn("[Apply] Invalid event. eventId={}, roomId={}, payload={}",
                    event.eventId(), event.roomId(), event.payload());
            return;
        }

        LOG.info("[Apply] Processing apply sent. eventId={}, roomId={}, applyId={}, campaignId={}",
                event.eventId(), event.roomId(), event.payload().applyId(), event.payload().campaignId());

        // 멱등성 키 결정 (deterministic: "APPLY_SENT:{applyId}")
        String idempotencyKey = event.eventId();

        // 이벤트 메타데이터 생성
        SystemEventMeta meta = new SystemEventMeta(
                idempotencyKey,
                event.roomId(),
                "ApplySentEvent"
        );

        // 컨텍스트 데이터 준비 (공통 키: messageKind, domainId)
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("messageKind", ChatSystemMessageKind.APPLY_CARD.toString());
        if (event.payload().applyId() != null) {
            contextData.put("domainId", event.payload().applyId());
            contextData.put("applyId", event.payload().applyId());
        }
        if (event.payload().campaignId() != null) {
            contextData.put("campaignId", event.payload().campaignId());
        }

        // 공통 템플릿 실행
        execute(
                meta,
                contextData,
                () -> event.payload(),
                payload -> {
                    boolean accepted = retrySender.sendWithIdempotency(
                            meta.idempotencyKey(),
                            meta.roomId(),
                            ChatSystemMessageKind.APPLY_CARD,
                            payload,
                            meta.eventType(),
                            contextData
                    );

                    if (accepted) {
                        LOG.info("[Apply] System message event accepted. idempotencyKey={}, roomId={}, applyId={}, kind={}",
                                meta.idempotencyKey(), meta.roomId(), event.payload().applyId(), ChatSystemMessageKind.APPLY_CARD);
                    } else {
                        LOG.warn("[Apply] System message event skipped (duplicate or logical failure). idempotencyKey={}, roomId={}, applyId={}, kind={}",
                                meta.idempotencyKey(), meta.roomId(), event.payload().applyId(), ChatSystemMessageKind.APPLY_CARD);
                    }
                }
        );
    }

    /**
     * 지원 상태 변경 이벤트 처리.
     * APPLY_STATUS_NOTICE 시스템 메시지를 전송합니다.
     */
    @Async
    public void handleApplyStatusChanged(ApplyStatusChangedEvent event) {
        // 채팅방 조회 (논리적 검증)
        Optional<Long> roomIdOpt = chatRoomQueryService.getRoomIdByUserPair(
                event.brandUserId(), event.creatorUserId());
        if (roomIdOpt.isEmpty()) {
            LOG.warn("[Apply] Chat room not found. eventId={}, applyId={}, brandUserId={}, creatorUserId={}",
                    event.eventId(), event.applyId(), event.brandUserId(), event.creatorUserId());
            return;
        }
        Long roomId = roomIdOpt.get();

        LOG.info("[Apply] Processing status change. eventId={}, applyId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                event.eventId(), event.applyId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

        // 멱등성 키 결정 (NOTICE suffix)
        String statusNoticeKey = String.format("%s:NOTICE", event.eventId());

        // 이벤트 메타데이터 생성
        SystemEventMeta meta = new SystemEventMeta(
                statusNoticeKey,
                roomId,
                "ApplyStatusChangedEvent"
        );

        // 컨텍스트 데이터 준비
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("messageKind", ChatSystemMessageKind.APPLY_STATUS_NOTICE.toString());
        if (event.applyId() != null) {
            contextData.put("domainId", event.applyId());
            contextData.put("applyId", event.applyId());
        }
        if (event.actorUserId() != null) {
            contextData.put("actorUserId", event.actorUserId());
        }

        // 공통 템플릿 실행
        execute(
                meta,
                contextData,
                () -> new ChatApplyStatusNoticePayloadResponse(
                        event.applyId(),
                        event.actorUserId(),
                        LocalDateTime.now()
                ),
                payload -> {
                    boolean accepted = retrySender.sendWithIdempotency(
                            meta.idempotencyKey(),
                            meta.roomId(),
                            ChatSystemMessageKind.APPLY_STATUS_NOTICE,
                            payload,
                            meta.eventType(),
                            contextData
                    );

                    if (accepted) {
                        LOG.info("[Apply] Status notice event accepted. idempotencyKey={}, roomId={}, applyId={}, status={}",
                                meta.idempotencyKey(), meta.roomId(), event.applyId(), event.newStatus());
                    } else {
                        LOG.warn("[Apply] Status notice event skipped (duplicate or logical failure). idempotencyKey={}, roomId={}, applyId={}, status={}",
                                meta.idempotencyKey(), meta.roomId(), event.applyId(), event.newStatus());
                    }
                }
        );
    }
}
