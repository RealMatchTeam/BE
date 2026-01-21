package com.example.RealMatch.chat.application.service.room;

import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.presentation.controller.fixture.ChatFixtureFactory;
import com.example.RealMatch.chat.presentation.dto.request.ChatRoomCreateRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    @Override
    public ChatRoomCreateResponse createOrGetRoom(CustomUserDetails user, ChatRoomCreateRequest request) {
        return ChatFixtureFactory.sampleRoomCreateResponse();
    }
}
