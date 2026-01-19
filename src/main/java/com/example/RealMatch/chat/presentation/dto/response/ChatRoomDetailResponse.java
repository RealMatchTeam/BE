package com.example.RealMatch.chat.presentation.dto.response;

import java.util.List;

import com.example.RealMatch.chat.presentation.dto.enums.ChatProposalStatus;

public record ChatRoomDetailResponse(
        Long roomId,
        Long opponentUserId,
        String opponentName,
        String opponentProfileImageUrl,
        List<String> opponentTags,
        ChatProposalStatus proposalStatus,
        String proposalStatusLabel
) {
}
