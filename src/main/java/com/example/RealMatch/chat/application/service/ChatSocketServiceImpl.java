package com.example.RealMatch.chat.application.service;

import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.presentation.controller.fixture.ChatSocketFixtureFactory;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

@Service
public class ChatSocketServiceImpl implements ChatSocketService {

    @Override
    @NonNull
    public ChatMessageCreatedEvent createMessageEvent(ChatSendMessageCommand command) {
        return Objects.requireNonNull(
                ChatSocketFixtureFactory.sampleMessageCreatedEvent(command),
                "ChatMessageCreatedEvent must not be null."
        );
    }

    @Override
    public ChatSendMessageAck createAck(ChatSendMessageCommand command, Long messageId) {
        return ChatSocketFixtureFactory.sampleAck(command, messageId);
    }

    @Override
    public ChatMessageCreatedEvent createSystemMessageEvent(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    ) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
