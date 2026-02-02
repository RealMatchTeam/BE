package com.example.RealMatch.match.domain.entity.enums;

public enum CampaignSortType {
    MATCH_SCORE,    // 매칭률 순 (동점 시 인기순 우선)
    POPULARITY,     // 인기 순 (좋아요 수)
    REWARD_AMOUNT,  // 금액 순 (원고료 높은 순)
    D_DAY           // 마감 순 (마감 임박순)
}
