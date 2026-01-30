package com.example.RealMatch.business.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.repository.CampaignProposalRepository;
import com.example.RealMatch.business.exception.BusinessErrorCode;
import com.example.RealMatch.business.presentation.dto.response.CampaignProposalDetailResponse;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignProposalQueryService {

    private final CampaignProposalRepository campaignProposalRepository;

    public CampaignProposalDetailResponse getProposalDetail(
            Long userId,
            UUID proposalId
    ) {
        CampaignProposal proposal = campaignProposalRepository.findByIdWithTags(proposalId)
                .orElseThrow(() -> new CustomException(BusinessErrorCode.CAMPAIGN_PROPOSAL_NOT_FOUND));

        return CampaignProposalDetailResponse.from(proposal);
    }
}
