package com.example.RealMatch.notification.domain.entity.enums;

public enum DeliveryStatus {
    PENDING,      // 발송 대기 (최초 생성)
    IN_PROGRESS,  // 발송 진행 중 (Consumer가 claim 점유)
    SENT,         // 발송 성공
    RETRY,        // 재시도 대기 (일시적 실패 후 backoff 대기)
    FAILED        // 발송 최종 실패 (재시도 불가)
}
