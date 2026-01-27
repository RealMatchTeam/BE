package com.example.RealMatch.chat.infrastructure.websocket;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.event.ChatMessageEventPublisher;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatRoomListUpdatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketChatMessageEventPublisher implements ChatMessageEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketChatMessageEventPublisher.class);
    private static final String ROOM_TOPIC_PREFIX = "/topic/rooms/";
    private static final String USER_ROOM_LIST_TOPIC_PREFIX = "/topic/user/";

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

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

    @Override
    public void publishRoomListUpdated(Long roomId) {
        Objects.requireNonNull(roomId, "roomId must not be null");

        try {
            // 채팅방의 모든 활성 멤버 조회
            List<ChatRoomMember> activeMembers = chatRoomMemberRepository.findByRoomId(roomId).stream()
                    .filter(member -> !member.isDeleted() && member.getLeftAt() == null)
                    .toList();

            ChatRoomListUpdatedEvent event = new ChatRoomListUpdatedEvent(roomId);

            // 각 멤버에게 개별적으로 채팅방 목록 업데이트 알림 발행
            for (ChatRoomMember member : activeMembers) {
                try {
                    String userTopic = USER_ROOM_LIST_TOPIC_PREFIX + member.getUserId() + "/rooms";
                    messagingTemplate.convertAndSend(userTopic, event);
                } catch (RuntimeException ex) {
                    LOG.warn("Failed to send room list update to user. roomId={}, userId={}", 
                            roomId, member.getUserId(), ex);
                }
            }
        } catch (RuntimeException ex) {
            // 브로드캐스트 실패는 DB 저장에 영향을 주지 않도록 로깅만 수행
            LOG.error("Failed to broadcast room list update. roomId={}", roomId, ex);
        }
    }
}
