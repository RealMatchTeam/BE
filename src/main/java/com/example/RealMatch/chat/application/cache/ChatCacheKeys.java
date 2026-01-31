package com.example.RealMatch.chat.application.cache;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import com.example.RealMatch.chat.domain.enums.ChatRoomFilterStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatCacheKeys {

    private static final String ROOM_LIST_PREFIX = "chat:room-list:";
    private static final String ROOM_LIST_VERSION_PREFIX = "chat:room-list:version:";
    private static final String ROOM_DETAIL_PREFIX = "chat:room-detail:";
    private static final String ROOM_DETAIL_VERSION_PREFIX = "chat:room-detail:version:";

    public static String roomListKey(
            Long userId,
            long version,
            ChatRoomFilterStatus filterStatus,
            int size,
            String search
    ) {
        return ROOM_LIST_PREFIX
                + userId + ":"
                + version + ":"
                + filterKey(filterStatus) + ":"
                + size + ":"
                + searchKey(search);
    }

    public static String roomListVersionKey(Long userId) {
        return ROOM_LIST_VERSION_PREFIX + userId;
    }

    public static String roomDetailKey(Long roomId, long version, Long userId) {
        return ROOM_DETAIL_PREFIX + roomId + ":" + version + ":" + userId;
    }

    public static String roomDetailVersionKey(Long roomId) {
        return ROOM_DETAIL_VERSION_PREFIX + roomId;
    }

    private static String filterKey(ChatRoomFilterStatus filterStatus) {
        return filterStatus == null ? "LATEST" : filterStatus.name();
    }

    private static String searchKey(String search) {
        if (!StringUtils.hasText(search)) {
            return "NONE";
        }
        String normalized = search.trim().toLowerCase(Locale.ROOT);
        return DigestUtils.md5DigestAsHex(normalized.getBytes(StandardCharsets.UTF_8));
    }
}
