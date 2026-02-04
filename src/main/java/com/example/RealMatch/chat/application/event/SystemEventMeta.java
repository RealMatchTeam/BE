package com.example.RealMatch.chat.application.event;

/**
 * 시스템 이벤트의 메타데이터를 담는 record.
 * 이벤트 식별 및 추적, DLQ enqueue에 사용됩니다.
 * 
 * <p>주의: idempotencyKey는 deterministic하며 멱등성 체크에 사용됩니다.
 * 실제 이벤트 ID(UUID 등)와는 다릅니다.
 */
public record SystemEventMeta(
        String idempotencyKey,  // 멱등성 키 (deterministic, 예: "APPLY_SENT:123" 또는 "PROPOSAL_SENT:456")
        Long roomId,             // 채팅방 ID
        String eventType         // 이벤트 타입 (로깅 및 DLQ용, 예: "ApplySentEvent")
) {
}
