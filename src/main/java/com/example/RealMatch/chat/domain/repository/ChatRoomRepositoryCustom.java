package com.example.RealMatch.chat.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;

public interface ChatRoomRepositoryCustom {
    List<ChatRoom> findRoomsByUser(
            Long userId,
            ChatRoomTab tab,
            ChatRoomFilterStatus filterStatus,
            RoomCursorInfo cursorInfo,
            int size
    );

    long countUnreadMessagesByUserAndTab(Long userId, ChatRoomTab tab);

    Map<ChatRoomTab, Long> countUnreadMessagesByTabs(Long userId);

    Map<Long, Long> countUnreadMessagesByRoomIds(List<Long> roomIds, Long userId);

    record RoomCursorInfo(LocalDateTime lastMessageAt, Long roomId) {
    }
}
