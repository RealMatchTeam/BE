package com.example.RealMatch.chat.presentation.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.example.RealMatch.chat.application.service.ChatSocketService;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;
import com.example.RealMatch.chat.presentation.resolver.ChatUserIdResolver;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;

@Controller
public class ChatSocketController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatSocketController.class);

    private final ChatSocketService chatSocketService;
    private final ChatUserIdResolver chatUserIdResolver;

    public ChatSocketController(
            ChatSocketService chatSocketService,
            ChatUserIdResolver chatUserIdResolver
    ) {
        this.chatSocketService = chatSocketService;
        this.chatUserIdResolver = chatUserIdResolver;
    }

    @MessageMapping("/chat.send")
    @SendToUser("/queue/chat.ack")
    public ChatSendMessageAck sendMessage(@Valid @Payload ChatSendMessageCommand command, Principal principal) {
        try {
            Long senderId = chatUserIdResolver.resolve(principal);
            ChatMessageResponse response = chatSocketService.createMessageEvent(command, senderId);
            return chatSocketService.createAck(command, response.messageId());
        } catch (ConstraintViolationException | IllegalArgumentException ex) {
            LOG.warn("Invalid chat send request. clientMessageId={}", command.clientMessageId(), ex);
            return chatSocketService.createFailedAck(command);
        } catch (RuntimeException ex) {
            LOG.error("Failed to process chat send request. clientMessageId={}", command.clientMessageId(), ex);
            return chatSocketService.createFailedAck(command);
        }
    }
}
