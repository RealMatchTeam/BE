package com.example.RealMatch.chat.presentation.dto.response;

public record ChatRoomDetailResponse(
        Long roomId,
        Long opponentUserId,
        String opponentName,
        String opponentProfileImageUrl,
        boolean isCollaborating,  // 협업중 여부
        CampaignSummaryResponse campaignSummary,  // 협업 요약 바 정보 (제안이 있는 경우에만 null이 아님)
        Long latestProposalId  // 재제안 폼 진입용. 해당 채팅방의 최신 PROPOSAL_CARD/RE_PROPOSAL_CARD 메시지의 proposalId (제안 없으면 null)
) {
}
