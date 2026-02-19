package com.example.RealMatch.business.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;
import com.example.RealMatch.brand.domain.repository.BrandAvailableSponsorRepository;
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
    private final BrandAvailableSponsorRepository brandAvailableSponsorRepository;

    public CampaignProposalDetailResponse getProposalDetail(
            Long userId,
            Long proposalId
    ) {
        CampaignProposal proposal = campaignProposalRepository.findByIdWithTags(proposalId)
                .orElseThrow(() -> new CustomException(BusinessErrorCode.CAMPAIGN_PROPOSAL_NOT_FOUND));
        
        // TODO: 데모데이 이후에 해당 제품이 없으면 에러 던지는 방향으로 수정 필요!! + 조회 권한 로직 추가 필요(본인만 조회 가능)
        String productName = Optional.ofNullable(proposal.getProductId())
                .flatMap(productId -> brandAvailableSponsorRepository
                        .findByBrandIdAndId(proposal.getBrand().getId(), productId))
                .map(BrandAvailableSponsor::getName)
                .orElse(null);

        return CampaignProposalDetailResponse.from(proposal, productName);
    }
}
