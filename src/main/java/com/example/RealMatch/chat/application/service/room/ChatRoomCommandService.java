package com.example.RealMatch.chat.application.service.room;

import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;

public interface ChatRoomCommandService {
    ChatRoomCreateResponse createOrGetRoom(Long userId, Long brandId, Long creatorId);
}
