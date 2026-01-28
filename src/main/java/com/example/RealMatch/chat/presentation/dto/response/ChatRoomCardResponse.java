package com.example.RealMatch.chat.presentation.dto.response;

import java.time.LocalDateTime;

import com.example.RealMatch.chat.domain.enums.ChatMessageType;

public record ChatRoomCardResponse(
        Long roomId,
        Long opponentUserId,
        String opponentName,
        String opponentProfileImageUrl,
        boolean isCollaborating,  // 협업중 여부 (협업중일 때만 true)
        String lastMessagePreview,
        ChatMessageType lastMessageType,
        LocalDateTime lastMessageAt,
        long unreadCount
) {
}
