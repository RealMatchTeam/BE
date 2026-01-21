package com.example.RealMatch.chat.presentation.dto.response;

import java.util.List;

import com.example.RealMatch.chat.application.conversion.RoomCursor;

public record ChatRoomListResponse(
        long sentTabUnreadCount,
        long receivedTabUnreadCount,
        Long totalUnreadCount,
        List<ChatRoomCardResponse> rooms,
        RoomCursor nextCursor,
        boolean hasNext
) {
}
