package com.example.RealMatch.chat.application.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.RealMatch.business.application.event.CampaignProposalStatusChangedEvent;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;

import lombok.RequiredArgsConstructor;

/**
 * 비즈니스 모듈에서 발행하는 CampaignProposalStatusChangedEvent를 수신하여
 * 채팅 모듈 내부 이벤트(ProposalStatusChangedEvent)로 변환하여 발행합니다.
 */
@Component
@RequiredArgsConstructor
public class CampaignProposalStatusChangedEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(CampaignProposalStatusChangedEventListener.class);

    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCampaignProposalStatusChanged(CampaignProposalStatusChangedEvent event) {
        if (event == null) {
            LOG.warn("Invalid CampaignProposalStatusChangedEvent: event is null");
            return;
        }

        LOG.info("CampaignProposalStatusChangedEvent received. proposalId={}, newStatus={}",
                event.proposalId(), event.newStatus());

        ChatProposalStatus chatStatus = toChatProposalStatus(event.newStatus());
        ProposalStatusChangedEvent chatEvent = new ProposalStatusChangedEvent(
                event.proposalId(),
                event.campaignId(),
                event.brandUserId(),
                event.creatorUserId(),
                chatStatus
        );
        eventPublisher.publishEvent(chatEvent);

        LOG.info("ProposalStatusChangedEvent published. proposalId={}, newStatus={}",
                event.proposalId(), chatStatus);
    }

    private static ChatProposalStatus toChatProposalStatus(ProposalStatus status) {
        if (status == null) {
            return ChatProposalStatus.NONE;
        }
        return switch (status) {
            case NONE -> ChatProposalStatus.NONE;
            case REVIEWING -> ChatProposalStatus.REVIEWING;
            case MATCHED -> ChatProposalStatus.MATCHED;
            case REJECTED -> ChatProposalStatus.REJECTED;
        };
    }
}
