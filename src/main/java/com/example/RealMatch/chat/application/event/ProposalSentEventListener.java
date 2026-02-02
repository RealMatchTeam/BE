package com.example.RealMatch.chat.application.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProposalSentEventListener {

    private final ChatProposalEventAsyncHandler asyncHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProposalSent(ProposalSentEvent event) {
        asyncHandler.handleProposalSent(event);
    }
}
