package com.example.RealMatch.chat.presentation.dto.response;

import java.time.LocalDateTime;

import com.example.RealMatch.chat.presentation.dto.enums.ChatMessageType;
import com.example.RealMatch.chat.presentation.dto.enums.ChatProposalStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;

public record ChatRoomCardResponse(
        Long roomId,
        Long opponentUserId,
        String opponentName,
        String opponentProfileImageUrl,
        ChatProposalStatus proposalStatus,
        String lastMessagePreview,
        ChatMessageType lastMessageType,
        LocalDateTime lastMessageAt,
        int unreadCount,
        ChatRoomTab tabCategory
) {
}
