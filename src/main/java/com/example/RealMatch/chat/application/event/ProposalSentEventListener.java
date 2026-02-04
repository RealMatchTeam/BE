package com.example.RealMatch.chat.application.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProposalSentEventListener {

    private final ChatProposalEventAsyncHandler asyncHandler;

    @EventListener
    public void handleProposalSent(ProposalSentEvent event) {
        asyncHandler.handleProposalSent(event);
    }
}
