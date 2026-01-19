package com.example.RealMatch.chat.application.service;

import org.springframework.web.multipart.MultipartFile;

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
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;

public interface ChatService {
    ChatRoomCreateResponse createOrGetRoom(CustomUserDetails user, ChatRoomCreateRequest request);

    ChatRoomListResponse getRoomList(
            CustomUserDetails user,
            ChatRoomTab tab,
            ChatRoomFilterStatus filterStatus,
            ChatRoomSort sort,
            RoomCursor roomCursor,
            int size
    );

    ChatRoomDetailResponse getRoomDetail(CustomUserDetails user, Long roomId);

    ChatMessageListResponse getMessages(
            CustomUserDetails user,
            Long roomId,
            MessageCursor messageCursor,
            int size
    );

    ChatAttachmentUploadResponse uploadAttachment(
            CustomUserDetails user,
            ChatAttachmentUploadRequest request,
            MultipartFile file
    );

    ChatMessageResponse createSystemMessage(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    );
}
