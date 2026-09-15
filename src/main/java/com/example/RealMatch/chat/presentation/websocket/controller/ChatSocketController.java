package com.example.RealMatch.chat.presentation.websocket.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.example.RealMatch.chat.application.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.application.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.application.dto.websocket.ChatSendMessageCommand;
import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.application.service.room.ChatRoomReadService;
import com.example.RealMatch.chat.presentation.resolver.ChatUserIdResolver;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatSocketController.class);

    private final ChatMessageSocketService chatMessageSocketService;
    private final ChatUserIdResolver chatUserIdResolver;
    private final ChatRoomReadService reads;

    @MessageMapping("/v1/chat.send")
    @SendToUser(value = "/queue/v1/chat.ack", broadcast = false)
    public ChatSendMessageAck sendMessage(@Valid @Payload ChatSendMessageCommand command, Principal principal) {
        if (command == null) {
            LOG.warn("Chat send received null command");
            return ChatSendMessageAck.failure(null, GeneralErrorCode.BAD_REQUEST);
        }
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

    @MessageExceptionHandler({
            MethodArgumentNotValidException.class,
            MessageConversionException.class})
    @SendToUser(value = "/queue/v1/chat.ack", broadcast = false)
    public ChatSendMessageAck invalidMessage(Exception exception) {
        return ChatSendMessageAck.failure(null, GeneralErrorCode.BAD_REQUEST);
    }

    @MessageMapping("/v1/chat.read")
    @SendToUser(value = "/queue/v1/chat.read", broadcast = false)
    public ReadAck markRead(@Valid @Payload ReadCommand command, Principal principal) {
        try {
            reads.markRead(command.roomId(), chatUserIdResolver.resolve(principal), command.messageId());
            return new ReadAck(command.roomId(), command.messageId(), true, null);
        } catch (CustomException ex) {
            return new ReadAck(command.roomId(), command.messageId(), false, ex.getCode().getCode());
        } catch (RuntimeException ex) {
            LOG.error("Chat read failed. roomId={}, messageId={}", command.roomId(), command.messageId(), ex);
            return new ReadAck(command.roomId(), command.messageId(), false, GeneralErrorCode.INTERNAL_SERVER_ERROR.getCode());
        }
    }

    public record ReadCommand(@NotNull @Positive Long roomId,
                              @NotNull @Positive Long messageId) { }
    public record ReadAck(Long roomId, Long messageId, boolean success, String errorCode) { }
}
