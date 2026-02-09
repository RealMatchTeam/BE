package com.example.RealMatch.business.application.event;

/**
 * 캠페인이 자동으로 확정되었을 때 발행되는 이벤트
 */
public record AutoConfirmedEvent(
        Long campaignId,
        Long brandUserId,
        Long creatorUserId,
        String campaignName
) {
}
