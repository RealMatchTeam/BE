package com.example.RealMatch.chat.application.idempotency;

import java.time.Duration;

/**
 * 이벤트 중복 처리 방지를 위한 멱등성 저장소 인터페이스.
 * 
 * 동일한 이벤트 ID가 이미 처리되었는지 확인하고, 처리되지 않은 경우에만 마킹합니다.
 * Redis SETNX + TTL 패턴을 사용하여 멱등성을 보장합니다.
 */
public interface ProcessedEventStore {
    /**
     * 이벤트가 이미 처리되었는지 확인하고, 처리되지 않은 경우에만 마킹합니다.
     */
    boolean markIfNotProcessed(String eventId, Duration ttl);

    /**
     * 이벤트 처리 완료를 마킹합니다 (전송 성공 후 호출)
     */
    boolean markAsProcessed(String eventId, Duration ttl);

    /**
     * 이벤트 처리 마킹을 삭제합니다 (논리적 실패 시 호출)
     */
    void removeProcessed(String eventId);
}
