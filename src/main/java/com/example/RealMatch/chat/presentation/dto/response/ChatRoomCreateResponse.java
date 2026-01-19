package com.example.RealMatch.chat.presentation.dto.response;

import java.time.LocalDateTime;

import com.example.RealMatch.chat.presentation.dto.enums.ChatProposalDirection;

public record ChatRoomCreateResponse(
        Long roomId,
        String roomKey,
        ChatProposalDirection lastProposalDirection,
        LocalDateTime createdAt
) {
}
