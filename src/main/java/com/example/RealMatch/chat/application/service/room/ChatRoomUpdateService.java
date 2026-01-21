package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.springframework.lang.NonNull;

import com.example.RealMatch.chat.presentation.dto.enums.ChatMessageType;

public interface ChatRoomUpdateService {
    void updateLastMessage(
            @NonNull Long roomId,
            @NonNull Long messageId,
            LocalDateTime messageAt,
            ChatMessageType messageType,
            String messagePreview
    );
}
