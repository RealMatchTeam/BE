package com.example.RealMatch.chat.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.RealMatch.chat.presentation.controller.fixture.ChatFixtureFactory;
import com.example.RealMatch.chat.presentation.conversion.MessageCursor;
import com.example.RealMatch.chat.presentation.conversion.RoomCursor;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomSort;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;
import com.example.RealMatch.chat.presentation.dto.request.ChatAttachmentUploadRequest;
import com.example.RealMatch.chat.presentation.dto.request.ChatRoomCreateRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatAttachmentUploadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageListResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.chat.presentation.swagger.ChatSwagger;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController implements ChatSwagger {

    @PostMapping("/rooms")
    public CustomResponse<ChatRoomCreateResponse> createOrGetRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        return CustomResponse.ok(ChatFixtureFactory.sampleRoomCreateResponse());
    }

    @GetMapping("/rooms")
    public CustomResponse<ChatRoomListResponse> getRoomList(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) ChatRoomTab tab,
            @RequestParam(name = "status", required = false) ChatRoomFilterStatus filterStatus,
            @RequestParam(required = false) ChatRoomSort sort,
            @RequestParam(name = "cursor", required = false) RoomCursor roomCursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return CustomResponse.ok(ChatFixtureFactory.sampleRoomListResponse());
    }

    @GetMapping("/rooms/{roomId}")
    public CustomResponse<ChatRoomDetailResponse> getRoomDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId
    ) {
        return CustomResponse.ok(ChatFixtureFactory.sampleRoomDetailResponse(roomId));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public CustomResponse<ChatMessageListResponse> getMessages(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestParam(name = "cursor", required = false) MessageCursor messageCursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return CustomResponse.ok(ChatFixtureFactory.sampleMessageListResponse(roomId));
    }

    @PostMapping("/attachments")
    public CustomResponse<ChatAttachmentUploadResponse> uploadAttachment(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("request") ChatAttachmentUploadRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        return CustomResponse.ok(ChatFixtureFactory.sampleAttachmentUploadResponse(request, file));
    }
}
