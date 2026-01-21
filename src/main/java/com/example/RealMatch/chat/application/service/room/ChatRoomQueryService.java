package com.example.RealMatch.chat.application.service.room;

import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomSort;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;

public interface ChatRoomQueryService {
    ChatRoomListResponse getRoomList(
            CustomUserDetails user,
            ChatRoomTab tab,
            ChatRoomFilterStatus filterStatus,
            ChatRoomSort sort,
            RoomCursor roomCursor,
            int size
    );

    ChatRoomDetailResponse getRoomDetail(CustomUserDetails user, Long roomId);
}
