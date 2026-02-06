package com.example.RealMatch.chat.application.event.apply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.RealMatch.business.application.event.CampaignApplyStatusChangedEvent;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;

import lombok.RequiredArgsConstructor;

/**
 * 비즈니스 모듈에서 발행하는 CampaignApplyStatusChangedEvent를 수신하여
 * 채팅 모듈 내부 이벤트(ApplyStatusChangedEvent)로 변환하여 발행합니다.
 */
@Component
@RequiredArgsConstructor
public class CampaignApplyStatusChangedEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(CampaignApplyStatusChangedEventListener.class);

    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCampaignApplyStatusChanged(CampaignApplyStatusChangedEvent event) {
        if (event == null) {
            LOG.warn("[ApplyBoundary] Invalid CampaignApplyStatusChangedEvent: event is null");
            return;
        }

        LOG.info("[ApplyBoundary] Received business event. applyId={}, newStatus={}",
                event.applyId(), event.newStatus());

        ChatProposalStatus chatStatus = toChatProposalStatus(event.newStatus());
        String eventId = ApplyStatusChangedEvent.generateEventId(event.applyId(), chatStatus);

        ApplyStatusChangedEvent chatEvent = new ApplyStatusChangedEvent(
                eventId,
                event.applyId(),
                event.campaignId(),
                event.brandUserId(),
                event.creatorUserId(),
                chatStatus,
                event.actorUserId()
        );
        eventPublisher.publishEvent(chatEvent);

        LOG.info("[ApplyBoundary] Published internal event. eventId={}, applyId={}, newStatus={}",
                eventId, event.applyId(), chatStatus);
    }

    private static ChatProposalStatus toChatProposalStatus(ProposalStatus status) {
        if (status == null) {
            return ChatProposalStatus.NONE;
        }
        return switch (status) {
            case CANCELED -> ChatProposalStatus.CANCELED;
            case NONE -> ChatProposalStatus.NONE;
            case REVIEWING -> ChatProposalStatus.REVIEWING;
            case MATCHED -> ChatProposalStatus.MATCHED;
            case REJECTED -> ChatProposalStatus.REJECTED;
        };
    }
}
