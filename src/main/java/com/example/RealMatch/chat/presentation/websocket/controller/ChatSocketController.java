package com.example.RealMatch.chat.presentation.websocket.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;
import com.example.RealMatch.chat.presentation.resolver.ChatUserIdResolver;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatSocketController.class);

    private final ChatMessageSocketService chatMessageSocketService;
    private final ChatUserIdResolver chatUserIdResolver;

    @MessageMapping("/v1/chat.send")
    @SendToUser("/queue/v1/chat.ack")
    public ChatSendMessageAck sendMessage(@Valid @Payload ChatSendMessageCommand command, Principal principal) {
        try {
            Long senderId = chatUserIdResolver.resolve(principal);
            ChatMessageResponse response = chatMessageSocketService.sendMessage(command, senderId);
            return ChatSendMessageAck.success(command.clientMessageId(), response.messageId());
        } catch (CustomException ex) {
            LOG.warn("Chat domain exception. clientMessageId={}, errorCode={}", 
                    command.clientMessageId(), ex.getCode().getCode(), ex);
            return ChatSendMessageAck.failure(command.clientMessageId(), ex.getCode());
        } catch (RuntimeException ex) {
            LOG.error("Unexpected runtime exception in chat send request. clientMessageId={}", 
                    command.clientMessageId(), ex);
            return ChatSendMessageAck.failure(command.clientMessageId(), GeneralErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
