package com.example.RealMatch.chat.application.event;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.application.service.room.ChatRoomCommandService;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryService;
import com.example.RealMatch.chat.application.service.room.MatchedCampaignPayloadProvider;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalStatusNoticePayloadResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProposalStatusChangedEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalStatusChangedEventListener.class);

    private final ChatRoomCommandService chatRoomCommandService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatMessageSocketService chatMessageSocketService;
    private final MatchedCampaignPayloadProvider matchedCampaignPayloadProvider;

    @EventListener
    @Async
    @Transactional
    public void handleProposalStatusChanged(ProposalStatusChangedEvent event) {
        try {
            LOG.info("Proposal status changed event received. proposalId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                    event.proposalId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

            ChatProposalStatus chatStatus = convertToChatProposalStatus(event.newStatus());
            chatRoomCommandService.updateProposalStatusByUsers(
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

            if (event.newStatus() == ProposalStatus.MATCHED) {
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
                    event.proposalId(), event.brandUserId(), event.creatorUserId(), ex);
        }
    }

    private ChatProposalStatus convertToChatProposalStatus(ProposalStatus proposalStatus) {
        return switch (proposalStatus) {
            case NONE -> ChatProposalStatus.NONE;
            case REVIEWING -> ChatProposalStatus.REVIEWING;
            case MATCHED -> ChatProposalStatus.MATCHED;
            case REJECTED -> ChatProposalStatus.REJECTED;
        };
    }
}
