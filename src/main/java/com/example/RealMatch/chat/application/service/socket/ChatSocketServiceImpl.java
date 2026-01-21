package com.example.RealMatch.chat.application.service.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.RealMatch.chat.application.service.message.ChatMessageCommandService;
import com.example.RealMatch.global.presentation.code.BaseErrorCode;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

@Service
public class ChatSocketServiceImpl implements ChatSocketService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatSocketServiceImpl.class);
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
        
        // 트랜잭션 커밋 후 브로드캐스트 실행
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        broadcastMessage(command.roomId(), response);
                    }
                }
        );
        
        return response;
    }

    private void broadcastMessage(Long roomId, ChatMessageResponse message) {
        try {
            ChatMessageCreatedEvent event = new ChatMessageCreatedEvent(roomId, message);
            messagingTemplate.convertAndSend(ROOM_TOPIC_PREFIX + roomId, event);
        } catch (Exception ex) {
            // 브로드캐스트 실패는 DB 저장에 영향을 주지 않도록 로깅만 수행
            LOG.error("Failed to broadcast message. roomId={}, messageId={}", roomId, message.messageId(), ex);
            
            // TODO: PM 설계 확정 후 브로드캐스트 실패 시 실시간성 보장 방안 구현 필요
        }
    }

    @Override
    public ChatSendMessageAck createAck(ChatSendMessageCommand command, Long messageId) {
        return ChatSendMessageAck.success(command.clientMessageId(), messageId);
    }

    @Override
    public ChatSendMessageAck createFailedAck(ChatSendMessageCommand command, BaseErrorCode errorCode) {
        return ChatSendMessageAck.failure(command.clientMessageId(), errorCode);
    }

    @Override
    @Transactional
    public ChatMessageCreatedEvent createSystemMessageEvent(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    ) {
        ChatMessageResponse response = chatMessageCommandService.saveSystemMessage(roomId, kind, payload);
        
        // 트랜잭션 커밋 후 브로드캐스트 실행
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        broadcastMessage(roomId, response);
                    }
                }
        );
        
        return new ChatMessageCreatedEvent(roomId, response);
    }
}
