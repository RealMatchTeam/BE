package com.example.RealMatch.chat.presentation.controller;

import java.util.Objects;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.example.RealMatch.chat.presentation.controller.fixture.ChatSocketFixtureFactory;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

import jakarta.validation.Valid;

@Controller
public class ChatSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    @SendToUser("/queue/chat.ack")
    public ChatSendMessageAck sendMessage(@Valid @Payload ChatSendMessageCommand command) {
        ChatMessageCreatedEvent event = ChatSocketFixtureFactory.sampleMessageCreatedEvent(command);
        messagingTemplate.convertAndSend("/topic/rooms/" + command.roomId(), Objects.requireNonNull(event));
        return ChatSocketFixtureFactory.sampleAck(command, event.message().messageId());
    }
}
