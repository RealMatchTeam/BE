package com.example.RealMatch.chat.application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.RealMatch.chat.presentation.controller.fixture.ChatFixtureFactory;
import com.example.RealMatch.chat.presentation.conversion.MessageCursor;
import com.example.RealMatch.chat.presentation.conversion.RoomCursor;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomSort;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.request.ChatAttachmentUploadRequest;
import com.example.RealMatch.chat.presentation.dto.request.ChatRoomCreateRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatAttachmentUploadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageListResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;

@Service
public class ChatServiceImpl implements ChatService {

    @Override
    public ChatRoomCreateResponse createOrGetRoom(CustomUserDetails user, ChatRoomCreateRequest request) {
        return ChatFixtureFactory.sampleRoomCreateResponse();
    }

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

    @Override
    public ChatMessageListResponse getMessages(
            CustomUserDetails user,
            Long roomId,
            MessageCursor messageCursor,
            int size
    ) {
        return ChatFixtureFactory.sampleMessageListResponse(roomId);
    }

    @Override
    public ChatAttachmentUploadResponse uploadAttachment(
            CustomUserDetails user,
            ChatAttachmentUploadRequest request,
            MultipartFile file
    ) {
        return ChatFixtureFactory.sampleAttachmentUploadResponse(request, file);
    }

    @Override
    public ChatMessageResponse createSystemMessage(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    ) {
        return ChatFixtureFactory.sampleMessageListResponse(roomId)
                .messages()
                .stream()
                .filter(message -> message.systemMessage() != null && message.systemMessage().kind() == kind)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported system message kind: " + kind));
    }
}
