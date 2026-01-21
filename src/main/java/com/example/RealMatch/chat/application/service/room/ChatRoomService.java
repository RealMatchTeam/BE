package com.example.RealMatch.chat.application.service.room;

import com.example.RealMatch.chat.presentation.dto.request.ChatRoomCreateRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;

public interface ChatRoomService {
    ChatRoomCreateResponse createOrGetRoom(CustomUserDetails user, ChatRoomCreateRequest request);
}
