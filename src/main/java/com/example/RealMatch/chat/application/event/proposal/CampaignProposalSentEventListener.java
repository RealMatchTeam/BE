package com.example.RealMatch.chat.application.event.proposal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.RealMatch.business.application.event.CampaignProposalSentEvent;
import com.example.RealMatch.business.domain.enums.ProposalDirection;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.chat.application.service.room.ChatRoomCommandService;
import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;
import com.example.RealMatch.chat.presentation.dto.enums.ChatProposalDecisionStatus;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalCardPayloadResponse;

import lombok.RequiredArgsConstructor;

/**
 * 비즈니스 모듈에서 발행하는 CampaignProposalSentEvent를 수신하여
 * 채팅 모듈 내부 이벤트(ProposalSentEvent)로 변환하여 발행합니다.
 */
@Component
@RequiredArgsConstructor
public class CampaignProposalSentEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(CampaignProposalSentEventListener.class);

    private final ChatRoomCommandService chatRoomCommandService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCampaignProposalSent(CampaignProposalSentEvent event) {
        if (event == null) {
            LOG.warn("[ProposalBoundary] Invalid CampaignProposalSentEvent: event is null");
            return;
        }

        LOG.info("[ProposalBoundary] Received business event. proposalId={}, isReProposal={}",
                event.proposalId(), event.isReProposal());

        // 채팅방이 없으면 생성
        Long roomId = ensureRoomAndGetId(event.brandUserId(), event.creatorUserId());
        ChatProposalCardPayloadResponse payload = createPayload(event);
        String eventId = ProposalSentEvent.generateEventId(event.proposalId(), event.isReProposal());

        ProposalSentEvent chatEvent = new ProposalSentEvent(eventId, roomId, payload, event.isReProposal());
        eventPublisher.publishEvent(chatEvent);

        LOG.info("[ProposalBoundary] Published internal event. eventId={}, roomId={}, proposalId={}",
                eventId, roomId, event.proposalId());
    }

    private ChatProposalCardPayloadResponse createPayload(CampaignProposalSentEvent event) {
        ChatProposalDirection direction = toChatProposalDirection(event.proposalDirection());
        ChatProposalDecisionStatus status = toChatDecisionStatus(event.proposalStatus());

        return new ChatProposalCardPayloadResponse(
                event.proposalId(),
                event.campaignId(),
                event.campaignName(),
                event.campaignSummary(),
                status,
                direction
        );
    }

    private ChatProposalDecisionStatus toChatDecisionStatus(ProposalStatus proposalStatus) {
        if (proposalStatus == null) {
            return ChatProposalDecisionStatus.PENDING;
        }
        return switch (proposalStatus) {
            case CANCELED ->  ChatProposalDecisionStatus.CANCELED;
            case REVIEWING, NONE -> ChatProposalDecisionStatus.PENDING;
            case MATCHED -> ChatProposalDecisionStatus.ACCEPTED;
            case REJECTED -> ChatProposalDecisionStatus.REJECTED;
        };
    }

    /**
     * 채팅방이 없으면 생성하고, roomId를 반환합니다.
     * 이 리스너는 AFTER_COMMIT 컨텍스트에서 실행되므로 createOrGetRoom이 별도 트랜잭션으로 처리됩니다.
     */
    private Long ensureRoomAndGetId(Long brandUserId, Long creatorUserId) {
        return chatRoomCommandService
                .createOrGetRoom(brandUserId, brandUserId, creatorUserId)
                .roomId();
    }

    private ChatProposalDirection toChatProposalDirection(ProposalDirection direction) {
        if (direction == null) {
            return ChatProposalDirection.NONE;
        }
        return switch (direction) {
            case BRAND_TO_CREATOR -> ChatProposalDirection.BRAND_TO_CREATOR;
            case CREATOR_TO_BRAND -> ChatProposalDirection.CREATOR_TO_BRAND;
        };
    }
}
