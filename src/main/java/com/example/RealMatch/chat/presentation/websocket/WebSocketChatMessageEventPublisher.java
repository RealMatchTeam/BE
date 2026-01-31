package com.example.RealMatch.chat.presentation.websocket;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.event.ChatMessageEventPublisher;
import com.example.RealMatch.chat.application.service.room.ChatRoomMemberQueryService;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatRoomListUpdatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketChatMessageEventPublisher implements ChatMessageEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketChatMessageEventPublisher.class);
    private static final String ROOM_TOPIC_PREFIX = "/topic/v1/rooms/";
    private static final String USER_ROOM_LIST_TOPIC_PREFIX = "/topic/v1/user/";

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomMemberQueryService chatRoomMemberQueryService;

    @Override
    public void publishMessageCreated(Long roomId, ChatMessageResponse message) {
        Objects.requireNonNull(roomId, "roomId must not be null");
        Objects.requireNonNull(message, "message must not be null");

        try {
            ChatMessageCreatedEvent event = new ChatMessageCreatedEvent(roomId, message);
            messagingTemplate.convertAndSend(ROOM_TOPIC_PREFIX + roomId, event);
        } catch (RuntimeException ex) {
            LOG.error("Failed to broadcast message. roomId={}, messageId={}",
                    roomId, message.messageId(), ex);
        }
    }

    @Override
    public void publishRoomListUpdated(Long roomId) {
        Objects.requireNonNull(roomId, "roomId must not be null");

        try {
            List<Long> userIds = chatRoomMemberQueryService.findActiveMemberUserIds(roomId);
            ChatRoomListUpdatedEvent event = new ChatRoomListUpdatedEvent(roomId);

            for (Long userId : userIds) {
                try {
                    String userTopic = USER_ROOM_LIST_TOPIC_PREFIX + userId + "/rooms";
                    messagingTemplate.convertAndSend(userTopic, event);
                } catch (RuntimeException ex) {
                    LOG.warn("Failed to send room list update to user. roomId={}, userId={}",
                            roomId, userId, ex);
                }
            }
        } catch (RuntimeException ex) {
            LOG.error("Failed to broadcast room list update. roomId={}", roomId, ex);
        }
    }
}
