package com.example.RealMatch.chat.application.event;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.RealMatch.business.application.event.CampaignApplySentEvent;
import com.example.RealMatch.business.application.event.CampaignApplyStatusChangedEvent;
import com.example.RealMatch.business.application.event.CampaignProposalSentEvent;
import com.example.RealMatch.business.application.event.CampaignProposalStatusChangedEvent;
import com.example.RealMatch.business.domain.enums.ProposalDirection;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.chat.application.dto.enums.ChatProposalDecisionStatus;
import com.example.RealMatch.chat.application.dto.response.ChatApplyCardPayloadResponse;
import com.example.RealMatch.chat.application.dto.response.ChatApplyStatusNoticePayloadResponse;
import com.example.RealMatch.chat.application.dto.response.ChatProposalCardPayloadResponse;
import com.example.RealMatch.chat.application.dto.response.ChatProposalStatusNoticePayloadResponse;
import com.example.RealMatch.chat.application.service.message.ChatMessageCommandService;
import com.example.RealMatch.chat.application.service.room.ChatRoomCommandService;
import com.example.RealMatch.chat.application.service.room.MatchedCampaignPayloadProvider;
import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class CollaborationChatEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(CollaborationChatEventListener.class);

    private final ChatMessageCommandService messages;
    private final ChatRoomCommandService rooms;
    private final MatchedCampaignPayloadProvider matchedCampaigns;

    @Order(0)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCampaignApplySent(CampaignApplySentEvent event) {
        if (event == null) {
            LOG.warn("[Chat] Invalid CampaignApplySentEvent: event is null");
            return;
        }

        Long roomId = roomId(event.brandUserId(), event.creatorUserId());
        var payload = new ChatApplyCardPayloadResponse(
                event.applyId(),
                event.campaignId(),
                event.campaignName(),
                event.campaignDescription(),
                event.applyReason()
        );
        messages.saveSystemMessage(roomId, event.eventId(), ChatSystemMessageKind.APPLY_CARD, payload);
    }

    @Order(0)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCampaignApplyStatusChanged(CampaignApplyStatusChangedEvent event) {
        if (event == null) {
            LOG.warn("[Chat] Invalid CampaignApplyStatusChangedEvent: event is null");
            return;
        }

        Long roomId = roomId(event.brandUserId(), event.creatorUserId());
        var payload = new ChatApplyStatusNoticePayloadResponse(
                event.applyId(),
                event.actorUserId(),
                LocalDateTime.now(),
                toChatProposalStatus(event.newStatus())
        );
        messages.saveSystemMessage(
                roomId,
                event.eventId() + ":NOTICE",
                ChatSystemMessageKind.APPLY_STATUS_NOTICE,
                payload
        );
    }

    @Order(0)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCampaignProposalSent(CampaignProposalSentEvent event) {
        if (event == null) {
            LOG.warn("[Chat] Invalid CampaignProposalSentEvent: event is null");
            return;
        }

        Long roomId = roomId(event.brandUserId(), event.creatorUserId());
        var payload = new ChatProposalCardPayloadResponse(
                event.proposalId(),
                event.campaignId(),
                event.campaignName(),
                event.campaignSummary(),
                toChatDecisionStatus(event.proposalStatus()),
                toChatProposalDirection(event.proposalDirection())
        );
        ChatSystemMessageKind kind = event.isReProposal()
                ? ChatSystemMessageKind.RE_PROPOSAL_CARD
                : ChatSystemMessageKind.PROPOSAL_CARD;
        messages.saveSystemMessage(roomId, event.eventId(), kind, payload);
    }

    @Order(0)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCampaignProposalStatusChanged(CampaignProposalStatusChangedEvent event) {
        if (event == null) {
            LOG.warn("[Chat] Invalid CampaignProposalStatusChangedEvent: event is null");
            return;
        }

        Long roomId = roomId(event.brandUserId(), event.creatorUserId());
        ChatProposalStatus status = toChatProposalStatus(event.newStatus());
        messages.saveSystemMessage(
                roomId,
                event.eventId() + ":NOTICE",
                ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE,
                new ChatProposalStatusNoticePayloadResponse(
                        event.proposalId(),
                        event.actorUserId(),
                        LocalDateTime.now(),
                        status
                )
        );

        if (status == ChatProposalStatus.MATCHED && event.campaignId() != null) {
            messages.saveSystemMessage(
                    roomId,
                    event.eventId() + ":MATCHED_CARD",
                    ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD,
                    matchedCampaigns.getPayload(
                            event.campaignId(),
                            toChatProposalDirection(event.proposalDirection())
                    ).orElseThrow(() -> new IllegalStateException("Matched campaign does not exist"))
            );
        }
    }

    private Long roomId(Long brandUserId, Long creatorUserId) {
        return rooms.createOrGetRoomSystem(brandUserId, creatorUserId).roomId();
    }

    private static ChatProposalStatus toChatProposalStatus(ProposalStatus status) {
        return status == null ? ChatProposalStatus.NONE : ChatProposalStatus.valueOf(status.name());
    }

    private static ChatProposalDirection toChatProposalDirection(ProposalDirection direction) {
        return direction == null ? ChatProposalDirection.NONE : ChatProposalDirection.valueOf(direction.name());
    }

    private static ChatProposalDecisionStatus toChatDecisionStatus(ProposalStatus status) {
        if (status == null) {
            return ChatProposalDecisionStatus.PENDING;
        }
        return switch (status) {
            case CANCELED -> ChatProposalDecisionStatus.CANCELED;
            case REVIEWING, NONE -> ChatProposalDecisionStatus.PENDING;
            case MATCHED -> ChatProposalDecisionStatus.ACCEPTED;
            case REJECTED -> ChatProposalDecisionStatus.REJECTED;
        };
    }
}
