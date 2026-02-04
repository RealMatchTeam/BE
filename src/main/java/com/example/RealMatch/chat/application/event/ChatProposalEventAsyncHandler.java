package com.example.RealMatch.chat.application.event;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryService;
import com.example.RealMatch.chat.application.service.room.ChatRoomUpdateService;
import com.example.RealMatch.chat.application.service.room.MatchedCampaignPayloadProvider;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalStatusNoticePayloadResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatProposalEventAsyncHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ChatProposalEventAsyncHandler.class);

    private final ChatMessageSocketService chatMessageSocketService;
    private final ChatRoomUpdateService chatRoomUpdateService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final MatchedCampaignPayloadProvider matchedCampaignPayloadProvider;

    @Async
    public void handleProposalSent(ProposalSentEvent event) {
        try {
            if (event == null || event.roomId() == null || event.payload() == null) {
                LOG.warn("Invalid ProposalSentEvent. roomId={}, payload={}",
                        event != null ? event.roomId() : null,
                        event != null ? event.payload() : null);
                return;
            }

            LOG.info("Proposal sent event received. roomId={}, proposalId={}, isReProposal={}",
                    event.roomId(), event.payload().proposalId(), event.isReProposal());

            ChatSystemMessageKind messageKind = event.isReProposal()
                    ? ChatSystemMessageKind.RE_PROPOSAL_CARD
                    : ChatSystemMessageKind.PROPOSAL_CARD;

            chatMessageSocketService.sendSystemMessage(
                    event.roomId(),
                    messageKind,
                    event.payload()
            );

            LOG.info("Proposal card system message sent. roomId={}, proposalId={}, kind={}",
                    event.roomId(), event.payload().proposalId(), messageKind);
        } catch (Exception ex) {
            LOG.error("Failed to handle proposal sent. roomId={}, proposalId={}",
                    event != null ? event.roomId() : null,
                    event != null && event.payload() != null ? event.payload().proposalId() : null,
                    ex);
        }
    }

    @Async
    public void handleProposalStatusChanged(ProposalStatusChangedEvent event) {
        try {
            if (event == null) {
                LOG.warn("Invalid ProposalStatusChangedEvent. event is null");
                return;
            }

            LOG.info("Proposal status changed event received. proposalId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                    event.proposalId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

            ChatProposalStatus chatStatus = event.newStatus();
            chatRoomUpdateService.updateProposalStatusByUsers(
                    event.brandUserId(),
                    event.creatorUserId(),
                    chatStatus
            );

            Optional<Long> roomIdOpt = chatRoomQueryService.getRoomIdByUserPair(
                    event.brandUserId(), event.creatorUserId());
            if (roomIdOpt.isEmpty()) {
                LOG.debug("Chat room not found for system message. brandUserId={}, creatorUserId={}",
                        event.brandUserId(), event.creatorUserId());
                return;
            }
            Long roomId = roomIdOpt.get();

            ChatProposalStatusNoticePayloadResponse statusNoticePayload =
                    new ChatProposalStatusNoticePayloadResponse(
                            event.proposalId(),
                            null,
                            LocalDateTime.now()
                    );
            chatMessageSocketService.sendSystemMessage(
                    roomId,
                    ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE,
                    statusNoticePayload
            );

            if (event.newStatus() == ChatProposalStatus.MATCHED) {
                matchedCampaignPayloadProvider.getPayload(event.campaignId())
                        .ifPresent(payload -> chatMessageSocketService.sendSystemMessage(
                                roomId,
                                ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD,
                                payload
                        ));
            }

            LOG.info("Chat room proposal status and system messages sent. roomId={}, status={}",
                    roomId, chatStatus);
        } catch (Exception ex) {
            LOG.error("Failed to handle proposal status changed. proposalId={}, brandUserId={}, creatorUserId={}",
                    event != null ? event.proposalId() : null,
                    event != null ? event.brandUserId() : null,
                    event != null ? event.creatorUserId() : null,
                    ex);
        }
    }

}
