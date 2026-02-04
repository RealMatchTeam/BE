package com.example.RealMatch.business.domain.enums;

import com.example.RealMatch.user.domain.entity.enums.Role;

/**
 * 제안 방향 (누가 누구에게 제안했는지).
 * 이벤트/외부 연동 시 사용하며, chat 등 다른 모듈이 user.Role에 의존하지 않도록 합니다.
 */
public enum ProposalDirection {
    BRAND_TO_CREATOR,
    CREATOR_TO_BRAND;

    public static ProposalDirection fromWhoProposed(Role whoProposed) {
        if (whoProposed == null) {
            throw new IllegalArgumentException("whoProposed is required");
        }
        return switch (whoProposed) {
            case BRAND -> BRAND_TO_CREATOR;
            case CREATOR -> CREATOR_TO_BRAND;
            case ADMIN, GUEST -> throw new IllegalArgumentException(
                    "Proposal direction is only for BRAND or CREATOR, got: " + whoProposed);
        };
    }
}
