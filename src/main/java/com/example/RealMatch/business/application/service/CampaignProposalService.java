package com.example.RealMatch.business.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.entity.CampaignProposalContentTag;
import com.example.RealMatch.business.domain.repository.CampaignProposalRepository;
import com.example.RealMatch.business.presentation.dto.request.CampaignProposalRequestDto;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.campaign.exception.CampaignErrorCode;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.tag.domain.entity.TagContent;
import com.example.RealMatch.tag.domain.repository.TagContentRepository;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.presentation.code.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignProposalService {

    private final CampaignProposalRepository campaignProposalRepository;
    private final TagContentRepository tagContentRepository;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final CampaignRepository campaignRepository;

    public void requestCampaign(Long creatorId, CampaignProposalRequestDto request) {

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다."));

        Campaign campaign = null;
        if (request.getCampaignId() != null) {
            campaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(() -> new CustomException(CampaignErrorCode.CAMPAIGN_NOT_FOUND));
        }

        CampaignProposal proposal = CampaignProposal.builder()
                .creator(creator)
                .brand(brand)
                .campaign(campaign)
                .title(request.getCampaignName())
                .campaignDescription(request.getDescription())
                .rewardAmount(request.getRewardAmount())
                .productId(request.getProductId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        // 태그 매핑
        saveTags(proposal, request.getFormats());
        saveTags(proposal, request.getCategories());
        saveTags(proposal, request.getTones());
        saveTags(proposal, request.getInvolvements());
        saveTags(proposal, request.getUsageRanges());

        campaignProposalRepository.save(proposal);
    }

    private void saveTags(
            CampaignProposal proposal,
            List<CampaignProposalRequestDto.CampaignContentTagRequest> tagRequests
    ) {
        // 1. tagId(UUID) 수집
        List<UUID> tagIds = tagRequests.stream()
                .map(CampaignProposalRequestDto.CampaignContentTagRequest::id)
                .toList();

        // 2. 한 번에 조회
        List<TagContent> tagContents = tagContentRepository.findAllById(tagIds);

        if (tagContents.size() != tagIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 태그가 포함되어 있습니다.");
        }

        // 3. Map<UUID, TagContent> 변환
        Map<UUID, TagContent> tagMap = tagContents.stream()
                .collect(Collectors.toMap(
                        TagContent::getId,
                        Function.identity()
                ));

        // 4. 매핑
        for (CampaignProposalRequestDto.CampaignContentTagRequest tagRequest : tagRequests) {
            TagContent tag = tagMap.get(tagRequest.id());

            proposal.addContentTag(
                    CampaignProposalContentTag.create(
                            proposal,
                            tag,
                            tagRequest.customValue()
                    )
            );
        }
    }


//    @Transactional(readOnly = true)
//    public CampaignProposalDetailResponse getProposalDetail(UUID proposalId) {
//
//        CampaignProposal proposal = campaignProposalRepository.findById(proposalId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 캠페인 제안입니다."));
//
//        List<CampaignProposalContentTag> tags =
//                proposal.getContentTags(); // LAZY라도 TX 안이니 OK
//
//        return CampaignProposalDetailResponse.from(proposal, tags);
//    }
}
