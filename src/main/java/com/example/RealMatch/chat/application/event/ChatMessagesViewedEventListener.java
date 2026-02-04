package com.example.RealMatch.chat.application.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.service.room.ChatRoomMemberCommandService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatMessagesViewedEventListener {

    private final ChatRoomMemberCommandService chatRoomMemberCommandService;

    @EventListener
    public void handle(ChatMessagesViewedEvent event) {
        if (event.memberId() == null || event.latestMessageId() == null) {
            return;
        }
        chatRoomMemberCommandService.updateLastReadMessage(event.memberId(), event.latestMessageId());
    }
}
