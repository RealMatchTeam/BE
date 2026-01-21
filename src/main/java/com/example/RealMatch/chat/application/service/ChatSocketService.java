package com.example.RealMatch.chat.application.service;

import org.springframework.lang.NonNull;

import com.example.RealMatch.chat.presentation.dto.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

public interface ChatSocketService {
    @NonNull
    ChatMessageResponse createMessageEvent(ChatSendMessageCommand command, Long senderId);

    ChatSendMessageAck createAck(ChatSendMessageCommand command, Long messageId);

    ChatSendMessageAck createFailedAck(ChatSendMessageCommand command);

    ChatMessageCreatedEvent createSystemMessageEvent(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    );
}
