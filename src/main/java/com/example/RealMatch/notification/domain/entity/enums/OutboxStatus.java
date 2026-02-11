package com.example.RealMatch.notification.domain.entity.enums;

/**
 * NotificationOutbox의 MQ 발행 상태
 *
 * <p>상태 전이:
 * <pre>
 * PENDING ──claim──▶ SENDING ──발행 성공──▶ SENT
 *    ▲                  │
 *    └──발행 실패────────┘
 *                       │
 *                       └──retryCount ≥ MAX──▶ FAILED
 * </pre>
 */
public enum OutboxStatus {
    PENDING,   // 발행 대기
    SENDING,   // 발행 진행 중 (claim 완료)
    SENT,      // MQ 발행 성공
    FAILED     // MQ 발행 최종 실패 (운영자 개입 필요)
}
