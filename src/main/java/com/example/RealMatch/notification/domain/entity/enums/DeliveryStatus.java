package com.example.RealMatch.notification.domain.entity.enums;

public enum DeliveryStatus {
    PENDING,      // 발송 대기
    IN_PROGRESS,  // 발송 진행 중 (Phase 3에서 워커가 작업 점유 시 사용)
    SENT,         // 발송 성공
    FAILED        // 발송 실패
}
