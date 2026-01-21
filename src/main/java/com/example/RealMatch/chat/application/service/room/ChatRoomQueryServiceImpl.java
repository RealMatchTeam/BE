package com.example.RealMatch.chat.application.service.room;

import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.presentation.controller.fixture.ChatFixtureFactory;
import com.example.RealMatch.chat.presentation.conversion.RoomCursor;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomSort;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;

@Service
public class ChatRoomQueryServiceImpl implements ChatRoomQueryService {

    @Override
    public ChatRoomListResponse getRoomList(
            CustomUserDetails user,
            ChatRoomTab tab,
            ChatRoomFilterStatus filterStatus,
            ChatRoomSort sort,
            RoomCursor roomCursor,
            int size
    ) {
        return ChatFixtureFactory.sampleRoomListResponse();
    }

    @Override
    public ChatRoomDetailResponse getRoomDetail(CustomUserDetails user, Long roomId) {
        return ChatFixtureFactory.sampleRoomDetailResponse(roomId);
    }
}
