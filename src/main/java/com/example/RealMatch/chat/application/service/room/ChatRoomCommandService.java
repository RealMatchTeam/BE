package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.springframework.lang.NonNull;

import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;

public interface ChatRoomCommandService {
    ChatRoomCreateResponse createOrGetRoom(Long userId, Long brandId, Long creatorId);

    void updateLastMessage(
            @NonNull Long roomId,
            @NonNull Long messageId,
            LocalDateTime messageAt,
            ChatMessageType messageType,
            String messagePreview
    );

    void updateProposalStatusByUsers(
            @NonNull Long brandUserId,
            @NonNull Long creatorUserId,
            @NonNull ChatProposalStatus status
    );
}
