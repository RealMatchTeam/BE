package com.example.RealMatch.chat.application.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.RealMatch.chat.application.service.room.ChatRoomMemberCommandService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatMessagesViewedEventListener {

    private final ChatRoomMemberCommandService chatRoomMemberCommandService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatMessagesViewedEvent event) {
        if (event.memberId() == null || event.latestMessageId() == null) {
            return;
        }
        chatRoomMemberCommandService.updateLastReadMessage(event.memberId(), event.latestMessageId());
    }
}
