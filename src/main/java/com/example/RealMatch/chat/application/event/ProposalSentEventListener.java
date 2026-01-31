package com.example.RealMatch.chat.application.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProposalSentEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalSentEventListener.class);

    private final ChatMessageSocketService chatMessageSocketService;

    @EventListener
    @Async
    public void handleProposalSent(ProposalSentEvent event) {
        try {
            if (event.roomId() == null || event.payload() == null) {
                LOG.warn("Invalid ProposalSentEvent. roomId={}, payload={}", 
                        event.roomId(), event.payload());
                return;
            }

            LOG.info("Proposal sent event received. roomId={}, proposalId={}",
                    event.roomId(), event.payload().proposalId());

            chatMessageSocketService.sendSystemMessage(
                    event.roomId(),
                    ChatSystemMessageKind.PROPOSAL_CARD,
                    event.payload()
            );

            LOG.info("Proposal card system message sent. roomId={}, proposalId={}",
                    event.roomId(), event.payload().proposalId());
        } catch (Exception ex) {
            LOG.error("Failed to handle proposal sent. roomId={}, proposalId={}",
                    event.roomId(), event.payload() != null ? event.payload().proposalId() : null, ex);
        }
    }
}
