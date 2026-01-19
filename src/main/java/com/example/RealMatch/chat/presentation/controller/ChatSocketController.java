package com.example.RealMatch.chat.presentation.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.example.RealMatch.chat.application.service.ChatSocketService;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

import jakarta.validation.Valid;

@Controller
public class ChatSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatSocketService chatSocketService;

    public ChatSocketController(SimpMessagingTemplate messagingTemplate, ChatSocketService chatSocketService) {
        this.messagingTemplate = messagingTemplate;
        this.chatSocketService = chatSocketService;
    }

    @MessageMapping("/chat.send")
    @SendToUser("/queue/chat.ack")
    public ChatSendMessageAck sendMessage(@Valid @Payload ChatSendMessageCommand command) {
        ChatMessageCreatedEvent event = chatSocketService.createMessageEvent(command);
        messagingTemplate.convertAndSend("/topic/rooms/" + command.roomId(), event);
        return chatSocketService.createAck(command, event.message().messageId());
    }
}
