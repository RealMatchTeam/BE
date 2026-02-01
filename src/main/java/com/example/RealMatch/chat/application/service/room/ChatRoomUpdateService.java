package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.springframework.lang.NonNull;

import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;

public interface ChatRoomUpdateService {
    void updateLastMessage(
            @NonNull Long roomId,
            @NonNull Long messageId,
            LocalDateTime messageAt,
            String messagePreview,
            ChatMessageType messageType
    );

    void updateProposalStatusByUsers(
            @NonNull Long brandUserId,
            @NonNull Long creatorUserId,
            @NonNull ChatProposalStatus status
    );
}
