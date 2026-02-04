package com.example.RealMatch.chat.application.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProposalStatusChangedEventListener {

    private final ChatProposalEventAsyncHandler asyncHandler;

    @EventListener
    public void handleProposalStatusChanged(ProposalStatusChangedEvent event) {
        asyncHandler.handleProposalStatusChanged(event);
    }
}
