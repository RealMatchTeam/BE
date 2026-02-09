package com.example.RealMatch.business.application.event;

/**
 * 정산이 가능한 상태가 되었을 때 발행되는 이벤트
 */
public record SettlementReadyEvent(
        Long campaignId,
        Long creatorUserId,
        Long brandUserId,
        String campaignName,
        Long settlementAmount
) {
}
