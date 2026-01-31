package com.example.RealMatch.chat.application.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.service.room.ChatRoomMemberQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatCacheEvictor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomMemberQueryService chatRoomMemberQueryService;

    public void evictRoomListByUser(Long userId) {
        if (userId == null) {
            return;
        }
        deleteByPattern(ChatCacheKeys.roomListPrefix(userId));
    }

    public void evictRoomListByRoom(Long roomId) {
        if (roomId == null) {
            return;
        }
        List<Long> userIds = chatRoomMemberQueryService.findActiveMemberUserIds(roomId);
        for (Long userId : userIds) {
            evictRoomListByUser(userId);
        }
    }

    public void evictRoomDetailByRoom(Long roomId) {
        if (roomId == null) {
            return;
        }
        deleteByPattern(ChatCacheKeys.roomDetailPrefix(roomId));
    }

    private void deleteByPattern(String prefix) {
        String pattern = prefix + "*";
        Set<String> keys = scanKeys(pattern);
        if (keys.isEmpty()) {
            return;
        }
        redisTemplate.delete(keys);
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(200)
                .build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            cursor.forEachRemaining(keys::add);
        }
        return keys;
    }
}
