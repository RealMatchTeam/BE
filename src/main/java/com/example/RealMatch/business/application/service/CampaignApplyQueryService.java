package com.example.RealMatch.business.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.business.domain.entity.CampaignApply;
import com.example.RealMatch.business.domain.repository.CampaignApplyRepository;
import com.example.RealMatch.business.exception.BusinessErrorCode;
import com.example.RealMatch.business.presentation.dto.response.CampaignApplyDetailResponse;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignApplyQueryService {
    private final CampaignApplyRepository campaignApplyRepository;

    public CampaignApplyDetailResponse getMyApplyCampaignDetails(Long campaignId, Long userId) {
        CampaignApply apply = campaignApplyRepository
                .findByCampaignIdAndUserId(campaignId, userId)
                .orElseThrow(() ->
                        new CustomException(
                                BusinessErrorCode.CAMPAIGN_APPLY_NOT_FOUND
                        )
                );

        return new CampaignApplyDetailResponse(
                apply.getCampaign().getId(),
                apply.getCampaign().getTitle(),
                apply.getReason(),
                apply.getProposalStatus()
        );
    }
}
