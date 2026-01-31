package com.example.RealMatch.chat.application.cache;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.service.room.ChatRoomMemberQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatCacheEvictor {

    private final ChatCacheStore chatCacheStore;
    private final ChatRoomMemberQueryService chatRoomMemberQueryService;

    public void evictRoomListByUser(Long userId) {
        if (userId == null) {
            return;
        }
        chatCacheStore.bumpVersion(ChatCacheKeys.roomListVersionKey(userId));
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
        chatCacheStore.bumpVersion(ChatCacheKeys.roomDetailVersionKey(roomId));
    }
}
