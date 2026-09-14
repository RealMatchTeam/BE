package com.example.RealMatch.chat.application.event.apply;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 채팅 내부 이벤트(ApplyStatusChangedEvent)를 수신하여
 * 업무 트랜잭션 안에서 ApplySystemMessageHandler로 위임합니다.
 */
@Component
@RequiredArgsConstructor
public class ApplyStatusChangedEventListener {

    private final ApplySystemMessageHandler handler;

    @EventListener
    public void handleApplyStatusChanged(ApplyStatusChangedEvent event) {
        handler.handleApplyStatusChanged(event);
    }
}
