package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.springframework.lang.NonNull;

import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;
import com.example.RealMatch.chat.presentation.dto.request.ChatRoomCreateRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;

public interface ChatRoomCommandService {
    ChatRoomCreateResponse createOrGetRoom(Long userId, ChatRoomCreateRequest request);

    void updateLastMessage(
            @NonNull Long roomId,
            @NonNull Long messageId,
            LocalDateTime messageAt,
            ChatMessageType messageType,
            String messagePreview
    );

    void updateProposalDirection(@NonNull Long roomId, @NonNull ChatProposalDirection direction);
}
