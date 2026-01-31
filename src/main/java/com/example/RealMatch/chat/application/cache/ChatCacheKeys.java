package com.example.RealMatch.chat.application.cache;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.domain.enums.ChatRoomFilterStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatCacheKeys {

    private static final String ROOM_LIST_PREFIX = "chat:room-list:";
    private static final String ROOM_DETAIL_PREFIX = "chat:room-detail:";

    public static String roomListKey(
            Long userId,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size,
            String search
    ) {
        return roomListPrefix(userId)
                + filterKey(filterStatus) + ":"
                + cursorKey(roomCursor) + ":"
                + size + ":"
                + searchKey(search);
    }

    public static String roomListPrefix(Long userId) {
        return ROOM_LIST_PREFIX + userId + ":";
    }

    public static String roomDetailKey(Long roomId, Long userId) {
        return roomDetailPrefix(roomId) + userId;
    }

    public static String roomDetailPrefix(Long roomId) {
        return ROOM_DETAIL_PREFIX + roomId + ":";
    }

    private static String filterKey(ChatRoomFilterStatus filterStatus) {
        return filterStatus == null ? "LATEST" : filterStatus.name();
    }

    private static String cursorKey(RoomCursor roomCursor) {
        return roomCursor == null ? "NONE" : roomCursor.encode();
    }

    private static String searchKey(String search) {
        if (!StringUtils.hasText(search)) {
            return "NONE";
        }
        String normalized = search.trim().toLowerCase(Locale.ROOT);
        return DigestUtils.md5DigestAsHex(normalized.getBytes(StandardCharsets.UTF_8));
    }
}
