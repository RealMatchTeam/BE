package com.example.RealMatch.chat.application.idempotency;

import java.time.Duration;

/**
 * 이벤트 중복 처리 방지를 위한 멱등성 저장소 인터페이스.
 *
 * <p>동일한 이벤트 ID가 이미 처리되었는지 확인하고, 처리되지 않은 경우에만 마킹합니다.
 * Redis SETNX + TTL 패턴을 사용하여 멱등성을 보장합니다.
 *
 * <h3>계약</h3>
 * <ul>
 *   <li>markIfNotProcessed: true=선점 성공(처리 가능), false=중복만 의미.
 *       Redis 장애/응답 null/타임아웃 등 판단 불가 시 예외 throw → 호출자가 실패로 처리(DLQ/재시도)</li>
 *   <li>markAsProcessed: void, 실패 시 throw (전송 성공 후 호출, 실패 시 호출자가 DLQ 처리)</li>
 *   <li>removeProcessed: 논리적 실패 또는 전송 최종 실패(재시도 소진) 시 호출. 최종 실패 시 키 제거해야 재처리 시 유실 없음. 중간 일시적 실패에서만 호출 금지. 실패 시 swallow(로그만)</li>
 * </ul>
 */
public interface ProcessedEventStore {

    /**
     * 이벤트가 이미 처리되었는지 확인하고, 처리되지 않은 경우에만 마킹(선점)합니다.
     *
     * @return true=선점 성공(처리 가능), false=중복(이미 처리됨)
     * @throws com.example.RealMatch.chat.application.exception.IdempotencyStoreException Redis 장애, 응답 null, 타임아웃 등 판단 불가 시
     */
    boolean markIfNotProcessed(String eventId, Duration ttl);

    /**
     * 이벤트 처리 완료를 마킹합니다 (전송 성공 후 호출).
     *
     * @throws com.example.RealMatch.chat.application.exception.IdempotencyStoreException Redis 장애 등 저장 실패 시
     */
    void markAsProcessed(String eventId, Duration ttl);

    /**
     * 이벤트 처리 마킹을 삭제합니다.
     * 호출처: 논리적 실패(재시도해도 동일 결과), 전송 최종 실패(재시도 소진 후 DLQ 기록 시, 재처리 유실 방지).
     * 실패 시 swallow(로그만). TTL로 자동 만료되므로 치명적이지 않음.
     */
    void removeProcessed(String eventId);
}
