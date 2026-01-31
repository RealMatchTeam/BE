package com.example.RealMatch.chat.application.service.room;

import java.util.Optional;

import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.domain.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;

public interface ChatRoomQueryService {

    Optional<Long> getRoomIdByUserPair(Long brandUserId, Long creatorUserId);

    ChatRoomListResponse getRoomList(
            Long userId,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size,
            String search
    );

    ChatRoomDetailResponse getChatRoomDetailWithOpponent(Long userId, Long roomId);
}
