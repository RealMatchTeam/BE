package com.example.RealMatch.chat.application.service.room;

import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;

public interface ChatRoomQueryService {
    ChatRoomListResponse getRoomList(
            Long userId,
            ChatRoomTab tab,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size
    );

    ChatRoomDetailResponse getChatRoomDetailWithOpponent(Long userId, Long roomId);
}
