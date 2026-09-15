package com.example.RealMatch.chat.application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.enums.ChatRoomFilterStatus;

public interface ChatRoomRepositoryCustom {
    List<ChatRoom> findRoomsByUser(
            Long userId,
            ChatRoomFilterStatus filterStatus,
            RoomCursorInfo cursorInfo,
            int size,
            String search
    );

    Set<Long> findCollaboratingRoomIds(List<Long> roomIds);

    long countTotalUnreadMessages(Long userId);

    Map<Long, Long> countUnreadMessagesByUser(Long userId);

    record RoomCursorInfo(LocalDateTime lastMessageAt, Long roomId) {
    }
}
