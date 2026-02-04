package com.example.RealMatch.chat.application.event.apply;

import com.example.RealMatch.chat.presentation.dto.response.ChatApplyCardPayloadResponse;

/**
 * 채팅 모듈 내부에서 사용되는 지원 전송 이벤트.
 * 비즈니스 이벤트(CampaignApplySentEvent)를 변환한 결과물입니다.
 */
public record ApplySentEvent(
        String eventId,      // 이벤트 중복 처리용 고유 ID
        Long roomId,
        ChatApplyCardPayloadResponse payload
) {
    /**
     * 같은 applyId면 항상 같은 ID가 생성됩니다.
     */
    public static String generateEventId(Long applyId) {
        if (applyId == null) {
            throw new IllegalArgumentException("applyId cannot be null");
        }
        return String.format("APPLY_SENT:%d", applyId);
    }
}
