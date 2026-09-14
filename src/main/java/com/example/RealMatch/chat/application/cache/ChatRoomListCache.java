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

    public long currentVersion(Long userId) {
        return chatCacheStore.getVersion(ChatCacheKeys.roomListVersionKey(userId));
    }

    public Optional<ChatRoomListResponse> get(
            Long userId,
            long version,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size,
            String search
    ) {
        if (userId == null || roomCursor != null || version < 0) {
            return Optional.empty();
        }
        String key = ChatCacheKeys.roomListKey(userId, version, filterStatus, size, search);
        return chatCacheStore.get(key, ChatRoomListResponse.class);
    }

    public void put(
            Long userId,
            long version,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size,
            String search,
            ChatRoomListResponse response
    ) {
        if (userId == null || response == null || roomCursor != null || version < 0) {
            return;
        }
        String key = ChatCacheKeys.roomListKey(userId, version, filterStatus, size, search);
        chatCacheStore.set(key, response, ChatCachePolicy.ROOM_LIST_TTL);
    }
}
