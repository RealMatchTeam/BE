package com.example.RealMatch.business.application.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.entity.CampaignProposalTag;
import com.example.RealMatch.business.domain.repository.CampaignProposalRepository;
import com.example.RealMatch.business.presentation.dto.request.CampaignProposalRequestDto;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.campaign.exception.CampaignErrorCode;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.tag.domain.entity.Tag;
import com.example.RealMatch.tag.domain.repository.TagRepository;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.presentation.code.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignProposalService {

    private final CampaignProposalRepository campaignProposalRepository;
    private final TagRepository tagRepository;
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

        saveTags(proposal, request.getTags());

        campaignProposalRepository.save(proposal);
    }

    private void saveTags(
            CampaignProposal proposal,
            List<CampaignProposalRequestDto.TagRequest> tagRequests
    ) {
        if (tagRequests == null || tagRequests.isEmpty()) {
            return;
        }

        List<Long> tagIds = tagRequests.stream()
                .map(CampaignProposalRequestDto.TagRequest::id)
                .toList();

        List<Tag> tags = tagRepository.findAllById(tagIds);

        if (tags.size() != tagIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 태그가 포함되어 있습니다.");
        }

        Map<Long, Tag> tagMap = tags.stream()
                .collect(Collectors.toMap(
                        Tag::getId,
                        Function.identity()
                ));

        for (CampaignProposalRequestDto.TagRequest tagRequest : tagRequests) {
            Tag tag = tagMap.get(tagRequest.id());

            proposal.addTag(
                    CampaignProposalTag.create(
                            proposal,
                            tag,
                            tagRequest.customValue()
                    )
            );
        }
    }
}
