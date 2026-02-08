package com.example.RealMatch.business.presentation.dto.response;

import java.time.LocalDate;

import com.example.RealMatch.business.domain.enums.CollaborationType;
import com.example.RealMatch.business.domain.enums.ProposalStatus;

public record CollaborationResponse(
        Long campaignId,           // 캠페인 기반일 때만 채움
        Long proposalId,           // 제안 기반일 때만 채움

        String brandName,
        String thumbnailUrl,
        String title,
        ProposalStatus status,

        LocalDate startDate,
        LocalDate endDate,

        CollaborationType type  // APPLIED / SENT / RECEIVED
        ) {
}
