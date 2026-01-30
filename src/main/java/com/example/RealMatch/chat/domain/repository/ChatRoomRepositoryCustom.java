package com.example.RealMatch.chat.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;

public interface ChatRoomRepositoryCustom {
    List<ChatRoom> findRoomsByUser(
            Long userId,
            ChatRoomFilterStatus filterStatus,
            RoomCursorInfo cursorInfo,
            int size,
            String search
    );

    long countTotalUnreadMessages(Long userId);

    Map<Long, Long> countUnreadMessagesByRoomIds(List<Long> roomIds, Long userId);

    record RoomCursorInfo(LocalDateTime lastMessageAt, Long roomId) {
    }
}
