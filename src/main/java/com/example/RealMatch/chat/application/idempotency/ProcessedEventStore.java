package com.example.RealMatch.chat.application.idempotency;

import java.time.Duration;

/**
 * 이벤트 중복 처리 방지를 위한 멱등성 저장소 인터페이스.
 *
 * <p>2단계 상태머신으로 "처리중(IN_PROGRESS)"과 "처리완료(PROCESSED)"를 분리합니다.
 * <ul>
 *   <li>markIfNotProcessed: IN_PROGRESS 상태로 선점 (짧은 TTL).
 *       프로세스 다운 시 짧은 TTL로 자동 만료 → 재처리 가능 (조용한 유실 방지).
 *       true=선점 성공(처리 가능), false=중복(이미 처리중이거나 처리완료).
 *       Redis 장애/응답 null/타임아웃 등 판단 불가 시 예외 throw → 호출자가 실패로 처리(DLQ/재시도)</li>
 *   <li>markAsProcessed: PROCESSED 상태로 승격 (긴 TTL).
 *       전송 성공 후 호출하여 중복 처리를 장기간 방지.
 *       실패 시 throw (호출자가 DLQ 처리)</li>
 *   <li>removeProcessed: 논리적 실패 또는 전송 최종 실패(재시도 소진) 시 호출.
 *       최종 실패 시 키 제거해야 재처리 시 유실 없음. 중간 일시적 실패에서는 호출 금지.
 *       실패 시 swallow(로그만). TTL로 자동 만료되므로 치명적이지 않음.</li>
 * </ul>
 */
public interface ProcessedEventStore {

    /**
     * 이벤트가 이미 처리되었는지 확인하고, 처리되지 않은 경우에만 IN_PROGRESS 상태로 마킹(선점)합니다.
     *
     * <p>짧은 TTL(inProgressTtl)로 설정되어, 프로세스 다운 시 빠르게 만료되어 재처리 가능합니다.
     *
     * @param eventId 이벤트 고유 식별자
     * @param inProgressTtl IN_PROGRESS 상태의 TTL (짧게: 1~5분 권장)
     * @return true=선점 성공(처리 가능), false=중복(이미 처리중이거나 처리완료)
     * @throws com.example.RealMatch.chat.application.exception.IdempotencyStoreException
     *         Redis 장애, 응답 null, 타임아웃 등 판단 불가 시
     */
    boolean markIfNotProcessed(String eventId, Duration inProgressTtl);

    /**
     * 이벤트 처리 완료를 PROCESSED 상태로 마킹합니다 (전송 성공 후 호출).
     *
     * <p>긴 TTL(processedTtl)로 설정되어, 동일 이벤트의 중복 처리를 장기간 방지합니다.
     *
     * @param eventId 이벤트 고유 식별자
     * @param processedTtl PROCESSED 상태의 TTL (길게: 6시간~수일 권장)
     * @throws com.example.RealMatch.chat.application.exception.IdempotencyStoreException
     *         Redis 장애 등 저장 실패 시
     */
    void markAsProcessed(String eventId, Duration processedTtl);

    /**
     * 이벤트 처리 마킹을 삭제합니다.
     * 호출처: 논리적 실패(재시도해도 동일 결과), 전송 최종 실패(재시도 소진 후 DLQ 기록 시, 재처리 유실 방지).
     * 실패 시 swallow(로그만). TTL로 자동 만료되므로 치명적이지 않음.
     */
    void removeProcessed(String eventId);
}
