package com.example.RealMatch.chat.application.event;

import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;

public interface ChatMessageEventPublisher {

    void publishMessageCreated(Long roomId, ChatMessageResponse message);

    void publishRoomListUpdated(Long roomId);
}
