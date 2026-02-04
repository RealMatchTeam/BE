package com.example.RealMatch.chat.application.event.proposal;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 채팅 내부 이벤트(ProposalStatusChangedEvent)를 수신하여
 * 비동기 핸들러(ProposalSystemMessageHandler)로 위임합니다.
 */
@Component
@RequiredArgsConstructor
public class ProposalStatusChangedEventListener {

    private final ProposalSystemMessageHandler handler;

    @EventListener
    public void handleProposalStatusChanged(ProposalStatusChangedEvent event) {
        handler.handleProposalStatusChanged(event);
    }
}
