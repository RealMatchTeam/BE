package com.example.RealMatch.business.presentation.dto.response;

import java.time.LocalDate;

import com.example.RealMatch.business.domain.enums.CollaborationType;
import com.example.RealMatch.business.domain.enums.ProposalStatus;

/**
 * 내부 전용 dto
 * **/
public record CollaborationProjection(
        Long campaignId,
        Long proposalId,

        String brandName,
        String thumbnailS3Key,   // 내부 전용
        String title,
        ProposalStatus status,

        LocalDate startDate,
        LocalDate endDate,

        CollaborationType type
) {
}
