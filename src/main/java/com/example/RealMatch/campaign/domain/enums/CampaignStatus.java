package com.example.RealMatch.campaign.domain.enums;

public enum CampaignStatus {
    DRAFT,        // 생성만 됨 (proposal → 막 생성)
    ACTIVE,       // 실제 진행 중
    COMPLETED,    // 캠페인 종료
    CANCELLED     // 중간 취소
}

