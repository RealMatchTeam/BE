package com.example.RealMatch.chat.application.event.apply;

import java.util.UUID;

import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;

/**
 * 채팅 모듈 내부용 지원 상태 변경 이벤트.
 * 비즈니스 이벤트(CampaignApplyStatusChangedEvent)를 변환한 결과물입니다.
 */
public record ApplyStatusChangedEvent(
        String eventId,      // 이벤트 중복 처리용 고유 ID (UUID 기반)
        Long applyId,
        Long campaignId,
        Long brandUserId,
        Long creatorUserId,
        ChatProposalStatus newStatus,
        Long actorUserId     // 상태 변경을 수행한 사용자 ID (취소/수락/거절한 사용자)
) {
    public static String generateEventId(Long applyId, ChatProposalStatus newStatus) {
        if (applyId == null) {
            throw new IllegalArgumentException("applyId cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus cannot be null");
        }
        return String.format("APPLY_STATUS_CHANGED:%d:%s:%s", applyId, newStatus, UUID.randomUUID().toString());
    }
}
