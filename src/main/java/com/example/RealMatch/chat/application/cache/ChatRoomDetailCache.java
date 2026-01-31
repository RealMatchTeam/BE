package com.example.RealMatch.chat.application.cache;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomDetailCache {

    private final ChatCacheStore chatCacheStore;

    public Optional<ChatRoomDetailResponse> get(Long roomId, Long userId) {
        if (roomId == null || userId == null) {
            return Optional.empty();
        }
        long version = chatCacheStore.getVersion(ChatCacheKeys.roomDetailVersionKey(roomId));
        String key = ChatCacheKeys.roomDetailKey(roomId, version, userId);
        return chatCacheStore.get(key, ChatRoomDetailResponse.class);
    }

    public void put(Long roomId, Long userId, ChatRoomDetailResponse response) {
        if (roomId == null || userId == null || response == null) {
            return;
        }
        long version = chatCacheStore.getVersion(ChatCacheKeys.roomDetailVersionKey(roomId));
        String key = ChatCacheKeys.roomDetailKey(roomId, version, userId);
        chatCacheStore.set(key, response, ChatCachePolicy.ROOM_DETAIL_TTL);
    }
}
