package com.example.RealMatch.chat.application.event.apply;

import java.util.UUID;

import com.example.RealMatch.chat.presentation.dto.response.ChatApplyCardPayloadResponse;

/**
 * 채팅 모듈 내부에서 사용되는 지원 전송 이벤트.
 * 비즈니스 이벤트(CampaignApplySentEvent)를 변환한 결과물입니다.
 */
public record ApplySentEvent(
        String eventId,      // 이벤트 중복 처리용 고유 ID (UUID 기반)
        Long roomId,
        ChatApplyCardPayloadResponse payload
) {
    public static String generateEventId(Long applyId, Long campaignId) {
        if (applyId == null) {
            throw new IllegalArgumentException("applyId cannot be null");
        }
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId cannot be null");
        }
        return String.format("APPLY_SENT:%d:%d:%s", applyId, campaignId, UUID.randomUUID().toString());
    }
}
