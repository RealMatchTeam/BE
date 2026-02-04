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
 * 지원(Apply) 관련 시스템 메시지 처리를 담당하는 핸들러
 * 
 * <p>역할: 오케스트레이션 (payload 준비, roomId 찾기, 메시지 종류 결정, 멱등성 키 결정)
 * <p>실제 전송/재시도/멱등성/DLQ는 Base의 execute 메서드와 SystemMessageRetrySender가 담당합니다.
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

        // 3. eventMeta 생성
        SystemEventMeta meta = new SystemEventMeta(
                idempotencyKey,
                event.roomId(),
                "ApplySentEvent"
        );

        // 4. contextData 준비 (공통 키: messageKind, domainId 필수)
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("messageKind", ChatSystemMessageKind.APPLY_CARD.toString());
        if (event.payload().applyId() != null) {
            contextData.put("domainId", event.payload().applyId()); // 공통 키
            contextData.put("applyId", event.payload().applyId());
        }
        if (event.payload().campaignId() != null) {
            contextData.put("campaignId", event.payload().campaignId());
        }

        // 5. Base의 execute 메서드 사용 (payload 생성은 이미 event에 있으므로 supplier로 감싸기)
        execute(
                meta,
                contextData,
                () -> event.payload(), // payload는 이미 event에 있음
                payload -> {
                    boolean sent = retrySender.sendWithIdempotency(
                            meta.idempotencyKey(),
                            meta.roomId(),
                            ChatSystemMessageKind.APPLY_CARD,
                            payload,
                            meta.eventType(),
                            contextData
                    );

                    if (sent) {
                        LOG.info("[Apply] System message sent. roomId={}, applyId={}, campaignId={}",
                                meta.roomId(), event.payload().applyId(), event.payload().campaignId());
                    } else {
                        LOG.warn("[Apply] Duplicate event, skipped. idempotencyKey={}", meta.idempotencyKey());
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

        LOG.info("[Apply] Processing status change. eventId={}, applyId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                event.eventId(), event.applyId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

        // 2. 상태 변경 알림 메시지 전송 (별도 멱등성 키)
        // event.eventId()는 "APPLY_STATUS_CHANGED:{applyId}:{newStatus}" 형태로 유니크함
        // ":NOTICE"를 붙여 "APPLY_STATUS_CHANGED:{applyId}:{newStatus}:NOTICE" 형태로 구분
        String statusNoticeKey = String.format("%s:NOTICE", event.eventId());
        
        // 3. eventMeta 생성
        SystemEventMeta meta = new SystemEventMeta(
                statusNoticeKey,
                roomId,
                "ApplyStatusChangedEvent"
        );

        // 4. contextData 준비 (공통 키: messageKind, domainId 필수)
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("messageKind", ChatSystemMessageKind.APPLY_STATUS_NOTICE.toString());
        if (event.applyId() != null) {
            contextData.put("domainId", event.applyId()); // 공통 키
            contextData.put("applyId", event.applyId());
        }
        if (event.actorUserId() != null) {
            contextData.put("actorUserId", event.actorUserId());
        }

        // 5. Base의 execute 메서드 사용
        execute(
                meta,
                contextData,
                () -> new ChatApplyStatusNoticePayloadResponse(
                        event.applyId(),
                        event.actorUserId(),
                        LocalDateTime.now()
                ),
                payload -> {
                    retrySender.sendWithIdempotency(
                            meta.idempotencyKey(),
                            meta.roomId(),
                            ChatSystemMessageKind.APPLY_STATUS_NOTICE,
                            payload,
                            meta.eventType(),
                            contextData
                    );
                    LOG.info("[Apply] Status change processed. roomId={}, applyId={}, status={}",
                            roomId, event.applyId(), event.newStatus());
                }
        );
    }
}
