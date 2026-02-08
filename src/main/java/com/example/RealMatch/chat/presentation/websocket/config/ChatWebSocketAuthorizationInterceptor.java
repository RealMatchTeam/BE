package com.example.RealMatch.chat.presentation.websocket.config;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.service.room.ChatRoomMemberService;
import com.example.RealMatch.chat.presentation.resolver.ChatUserIdResolver;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatWebSocketAuthorizationInterceptor implements ChannelInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ChatWebSocketAuthorizationInterceptor.class);

    private static final Pattern ROOM_TOPIC_PATTERN =
            Pattern.compile("^/topic/v1/rooms/(\\d+)$");
    private static final Pattern USER_ROOM_LIST_PATTERN =
            Pattern.compile("^/topic/v1/user/(\\d+)/rooms$");
    private static final String USER_QUEUE_PREFIX = "/user/queue/";

    private final ChatRoomMemberService chatRoomMemberService;
    private final ChatUserIdResolver chatUserIdResolver;

    @Override
    public Message<?> preSend(@Nullable Message<?> message, @Nullable MessageChannel channel) {
        if (message == null) {
            return null;
        }

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == StompCommand.SUBSCRIBE) {
            authorizeSubscription(accessor);
        } else if (command == StompCommand.SEND) {
            requireAuthenticated(accessor);
        }

        return message;
    }

    // ── SUBSCRIBE 인가 ──────────────────────────────────────────────

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        Long userId = resolveUserId(accessor);
        String destination = accessor.getDestination();

        if (destination == null) {
            throw new MessageDeliveryException("Subscription destination is required");
        }

        // /topic/v1/rooms/{roomId} → 방 멤버 검증
        Matcher roomMatcher = ROOM_TOPIC_PATTERN.matcher(destination);
        if (roomMatcher.matches()) {
            Long roomId = Long.parseLong(roomMatcher.group(1));
            authorizeRoomSubscription(roomId, userId);
            return;
        }

        // /topic/v1/user/{userId}/rooms → 본인 userId 일치 검증
        Matcher userRoomListMatcher = USER_ROOM_LIST_PATTERN.matcher(destination);
        if (userRoomListMatcher.matches()) {
            Long targetUserId = Long.parseLong(userRoomListMatcher.group(1));
            authorizeUserRoomListSubscription(targetUserId, userId);
            return;
        }

        if (destination.startsWith(USER_QUEUE_PREFIX)) {
            return;
        }

        // 그 외 → 거부
        LOG.warn("Unauthorized subscription attempt. userId={}, destination={}", userId, destination);
        throw new MessageDeliveryException("Unauthorized subscription destination: " + destination);
    }

    private void authorizeRoomSubscription(Long roomId, Long userId) {
        try {
            chatRoomMemberService.getActiveMemberOrThrow(roomId, userId);
        } catch (RuntimeException ex) {
            LOG.warn("Room subscription denied. userId={}, roomId={}, reason={}",
                    userId, roomId, ex.getMessage());
            throw new MessageDeliveryException(
                    "Not authorized to subscribe to room: " + roomId);
        }
    }

    private void authorizeUserRoomListSubscription(Long targetUserId, Long userId) {
        if (!targetUserId.equals(userId)) {
            LOG.warn("User room list subscription denied. userId={}, targetUserId={}",
                    userId, targetUserId);
            throw new MessageDeliveryException(
                    "Cannot subscribe to another user's room list");
        }
    }

    // ── SEND 인가 ───────────────────────────────────────────────────

    private void requireAuthenticated(StompHeaderAccessor accessor) {
        getPrincipalOrThrow(accessor);
    }

    // ── 공통 헬퍼 ───────────────────────────────────────────────────

    private Principal getPrincipalOrThrow(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new MessageDeliveryException("Authentication required");
        }
        return principal;
    }

    private Long resolveUserId(StompHeaderAccessor accessor) {
        Principal principal = getPrincipalOrThrow(accessor);
        try {
            return chatUserIdResolver.resolve(principal);
        } catch (RuntimeException ex) {
            throw new MessageDeliveryException("Failed to resolve user identity");
        }
    }
}
