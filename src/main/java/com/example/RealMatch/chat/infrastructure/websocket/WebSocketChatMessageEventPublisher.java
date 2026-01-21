package com.example.RealMatch.chat.infrastructure.websocket;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.event.ChatMessageEventPublisher;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;

@Component
public class WebSocketChatMessageEventPublisher implements ChatMessageEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketChatMessageEventPublisher.class);
    private static final String ROOM_TOPIC_PREFIX = "/topic/rooms/";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketChatMessageEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishMessageCreated(Long roomId, ChatMessageResponse message) {
        Objects.requireNonNull(roomId, "roomId must not be null");
        Objects.requireNonNull(message, "message must not be null");

        try {
            ChatMessageCreatedEvent event = new ChatMessageCreatedEvent(roomId, message);
            messagingTemplate.convertAndSend(ROOM_TOPIC_PREFIX + roomId, event);
        } catch (RuntimeException ex) {
            // 브로드캐스트 실패는 DB 저장에 영향을 주지 않도록 로깅만 수행
            LOG.error("Failed to broadcast message. roomId={}, messageId={}", 
                    roomId, message.messageId(), ex);

            // TODO: PM 설계 확정 후 브로드캐스트 실패 시 실시간성 보장 방안 구현 필요
        }
    }
}
