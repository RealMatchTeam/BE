package com.example.RealMatch.chat.application.service;

import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.service.message.ChatMessageCommandService;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSendMessageAckStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

@Service
public class ChatSocketServiceImpl implements ChatSocketService {

    private static final String ROOM_TOPIC_PREFIX = "/topic/rooms/";

    private final ChatMessageCommandService chatMessageCommandService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatSocketServiceImpl(
            ChatMessageCommandService chatMessageCommandService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.chatMessageCommandService = chatMessageCommandService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    @NonNull
    public ChatMessageResponse createMessageEvent(ChatSendMessageCommand command, Long senderId) {
        ChatMessageResponse response = chatMessageCommandService.saveMessage(command, senderId);
        broadcastMessage(command.roomId(), response);
        return response;
    }

    private void broadcastMessage(Long roomId, ChatMessageResponse message) {
        ChatMessageCreatedEvent event = new ChatMessageCreatedEvent(roomId, message);
        messagingTemplate.convertAndSend(ROOM_TOPIC_PREFIX + roomId, event);
    }

    @Override
    public ChatSendMessageAck createAck(ChatSendMessageCommand command, Long messageId) {
        return new ChatSendMessageAck(command.clientMessageId(), messageId, ChatSendMessageAckStatus.SUCCESS);
    }

    @Override
    public ChatSendMessageAck createFailedAck(ChatSendMessageCommand command) {
        return new ChatSendMessageAck(command.clientMessageId(), null, ChatSendMessageAckStatus.FAILED);
    }

    @Override
    @Transactional
    public ChatMessageCreatedEvent createSystemMessageEvent(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    ) {
        ChatMessageResponse response = chatMessageCommandService.saveSystemMessage(roomId, kind, payload);
        broadcastMessage(roomId, response);
        return new ChatMessageCreatedEvent(roomId, response);
    }
}
