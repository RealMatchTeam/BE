package com.example.RealMatch.match.domain.entity.enums;

public enum SortType {
    MATCH_SCORE,    // 매칭률 순 (동점 시 인기순 우선)
    POPULARITY,     // 인기 순
    REWARD_AMOUNT,  // 금액 순 (캠페인 원고료)
    D_DAY,          // 마감 순 (마감 임박순)
    NEWEST          // 신규순 (브랜드 API 호환용)
}
