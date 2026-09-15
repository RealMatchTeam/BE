package com.example.RealMatch.chat.application.dto.response;

import java.util.List;

import com.example.RealMatch.chat.application.conversion.RoomCursor;

public record ChatRoomListResponse(
        Long totalUnreadCount,
        List<ChatRoomCardResponse> rooms,
        RoomCursor nextCursor,
        boolean hasNext
) {
}
