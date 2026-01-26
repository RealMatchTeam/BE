package com.example.RealMatch.chat.application.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.chat.application.service.room.ChatRoomCommandService;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProposalStatusChangedEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalStatusChangedEventListener.class);

    private final ChatRoomCommandService chatRoomCommandService;

    @EventListener
    @Async
    @Transactional
    public void handleProposalStatusChanged(ProposalStatusChangedEvent event) {
        try {
            LOG.info("Proposal status changed event received. proposalId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                    event.proposalId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

            ChatProposalStatus chatStatus = convertToChatProposalStatus(event.newStatus());

            // 채팅방의 proposalStatus 업데이트
            chatRoomCommandService.updateProposalStatusByUsers(
                    event.brandUserId(),
                    event.creatorUserId(),
                    chatStatus
            );

            LOG.info("Chat room proposal status updated. brandUserId={}, creatorUserId={}, status={}",
                    event.brandUserId(), event.creatorUserId(), chatStatus);
        } catch (Exception ex) {
            LOG.error("Failed to update chat room proposal status. proposalId={}, brandUserId={}, creatorUserId={}",
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
