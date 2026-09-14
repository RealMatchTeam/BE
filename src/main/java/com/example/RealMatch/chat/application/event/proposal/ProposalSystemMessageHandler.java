package com.example.RealMatch.chat.application.event.proposal;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.service.message.ChatMessageCommandService;
import com.example.RealMatch.chat.application.service.room.ChatRoomCommandService;
import com.example.RealMatch.chat.application.service.room.ChatRoomUpdateService;
import com.example.RealMatch.chat.application.service.room.MatchedCampaignPayloadProvider;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalStatusNoticePayloadResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ProposalSystemMessageHandler {

    private final ChatMessageCommandService messageService;
    private final ChatRoomCommandService roomService;
    private final ChatRoomUpdateService roomUpdateService;
    private final MatchedCampaignPayloadProvider matchedCampaignPayloadProvider;
    private final ChatMessageRepository messageRepository;

    public void handleProposalSent(ProposalSentEvent event) {
        ChatSystemMessageKind kind = event.isReProposal()
                ? ChatSystemMessageKind.RE_PROPOSAL_CARD : ChatSystemMessageKind.PROPOSAL_CARD;
        messageService.saveSystemMessage(event.roomId(), event.eventId(), kind, event.payload());
    }

    public void handleProposalStatusChanged(ProposalStatusChangedEvent event) {
        Long roomId = roomService.createOrGetRoomSystem(event.brandUserId(), event.creatorUserId()).roomId();
        String noticeKey = event.eventId() + ":NOTICE";
        if (messageRepository.findByRoomIdAndSystemEventId(roomId, noticeKey).isPresent()) {
            return;
        }
        roomUpdateService.updateProposalStatusByUsers(
                event.brandUserId(), event.creatorUserId(), event.newStatus());
        messageService.saveSystemMessage(roomId, noticeKey, ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE,
                new ChatProposalStatusNoticePayloadResponse(event.proposalId(), event.actorUserId(),
                        LocalDateTime.now(), event.newStatus()));
        if (event.newStatus() == ChatProposalStatus.MATCHED && event.campaignId() != null) {
            messageService.saveSystemMessage(roomId, event.eventId() + ":MATCHED_CARD",
                    ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD,
                    matchedCampaignPayloadProvider.getPayload(event.campaignId(), event.proposalDirection())
                            .orElseThrow(() -> new IllegalStateException("Matched campaign does not exist")));
        }
    }
}
