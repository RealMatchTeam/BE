package com.example.RealMatch.business.application.event;

public record CampaignApplySentEvent(
        Long applyId,
        Long campaignId,
        Long creatorUserId,  // 지원한 크리에이터 ID
        Long brandUserId,    // 캠페인을 소유한 브랜드의 사용자 ID
        String campaignName,
        String campaignDescription,
        String applyReason   // 지원 사유
) {
}
