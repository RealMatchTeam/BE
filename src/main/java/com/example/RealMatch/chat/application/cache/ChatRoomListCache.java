package com.example.RealMatch.chat.application.cache;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.domain.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomListCache {

    private final ChatCacheStore chatCacheStore;

    public Optional<ChatRoomListResponse> get(
            Long userId,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size,
            String search
    ) {
        if (userId == null) {
            return Optional.empty();
        }
        String key = ChatCacheKeys.roomListKey(userId, filterStatus, roomCursor, size, search);
        return chatCacheStore.get(key, ChatRoomListResponse.class);
    }

    public void put(
            Long userId,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size,
            String search,
            ChatRoomListResponse response
    ) {
        if (userId == null || response == null) {
            return;
        }
        String key = ChatCacheKeys.roomListKey(userId, filterStatus, roomCursor, size, search);
        chatCacheStore.set(key, response, ChatCachePolicy.ROOM_LIST_TTL);
    }
}
