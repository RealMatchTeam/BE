package com.example.RealMatch.chat.application.event.apply;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.RealMatch.business.application.event.CampaignApplySentEvent;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryService;
import com.example.RealMatch.chat.presentation.dto.response.ChatApplyCardPayloadResponse;

import lombok.RequiredArgsConstructor;

/**
 * 비즈니스 모듈에서 발행하는 CampaignApplySentEvent를 수신하여
 * 채팅 모듈 내부 이벤트(ApplySentEvent)로 변환하여 발행합니다.
 */
@Component
@RequiredArgsConstructor
public class CampaignApplySentEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(CampaignApplySentEventListener.class);

    private final ChatRoomQueryService chatRoomQueryService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCampaignApplySent(CampaignApplySentEvent event) {
        if (event == null) {
            LOG.warn("[ApplyBoundary] Invalid CampaignApplySentEvent: event is null");
            return;
        }

        LOG.info("[ApplyBoundary] Received business event. applyId={}, campaignId={}, creatorUserId={}, brandUserId={}",
                event.applyId(), event.campaignId(), event.creatorUserId(), event.brandUserId());

        Optional<Long> roomIdOpt = chatRoomQueryService.getRoomIdByUserPair(
                event.brandUserId(), event.creatorUserId());

        if (roomIdOpt.isEmpty()) {
            LOG.debug("[ApplyBoundary] Chat room not found. brandUserId={}, creatorUserId={}",
                    event.brandUserId(), event.creatorUserId());
            return;
        }

        Long roomId = roomIdOpt.get();
        ChatApplyCardPayloadResponse payload = createPayload(event);
        String eventId = ApplySentEvent.generateEventId(event.applyId(), event.campaignId());

        ApplySentEvent chatEvent = new ApplySentEvent(eventId, roomId, payload);
        eventPublisher.publishEvent(chatEvent);

        LOG.info("[ApplyBoundary] Published internal event. eventId={}, roomId={}, applyId={}",
                eventId, roomId, event.applyId());
    }

    private ChatApplyCardPayloadResponse createPayload(CampaignApplySentEvent event) {
        return new ChatApplyCardPayloadResponse(
                event.applyId(),
                event.campaignId(),
                event.campaignName(),
                event.campaignDescription(),
                event.applyReason()
        );
    }
}
