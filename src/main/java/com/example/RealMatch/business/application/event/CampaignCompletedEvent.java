package com.example.RealMatch.business.application.event;

/**
 * 캠페인이 완료되었을 때 발행되는 이벤트
 */
public record CampaignCompletedEvent(
        Long campaignId,
        Long brandUserId,
        Long creatorUserId,
        String campaignName
) {
}
