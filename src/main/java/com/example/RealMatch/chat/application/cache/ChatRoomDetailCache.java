package com.example.RealMatch.chat.application.cache;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomDetailCache {

    private final ChatCacheStore chatCacheStore;

    public long currentVersion(Long roomId) {
        return chatCacheStore.getVersion(ChatCacheKeys.roomDetailVersionKey(roomId));
    }

    public Optional<ChatRoomDetailResponse> get(Long roomId, Long userId, long version) {
        if (roomId == null || userId == null || version < 0) {
            return Optional.empty();
        }
        String key = ChatCacheKeys.roomDetailKey(roomId, version, userId);
        return chatCacheStore.get(key, ChatRoomDetailResponse.class);
    }

    public void put(Long roomId, Long userId, long version, ChatRoomDetailResponse response) {
        if (roomId == null || userId == null || response == null || version < 0) {
            return;
        }
        String key = ChatCacheKeys.roomDetailKey(roomId, version, userId);
        chatCacheStore.set(key, response, ChatCachePolicy.ROOM_DETAIL_TTL);
    }
}
