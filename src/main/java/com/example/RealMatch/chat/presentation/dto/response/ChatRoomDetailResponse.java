package com.example.RealMatch.chat.presentation.dto.response;

import java.util.List;

public record ChatRoomDetailResponse(
        Long roomId,
        Long opponentUserId,
        String opponentName,
        String opponentProfileImageUrl,
        List<String> opponentTags,
        boolean isCollaborating,  // 협업중 여부
        CampaignSummaryResponse campaignSummary  // 협업 요약 바 정보 (제안이 있는 경우에만 null이 아님)
) {
}
