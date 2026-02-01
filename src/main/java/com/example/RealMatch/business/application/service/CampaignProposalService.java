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
import com.example.RealMatch.business.exception.BusinessErrorCode;
import com.example.RealMatch.business.presentation.dto.request.CampaignProposalRequestDto;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.campaign.exception.CampaignErrorCode;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;
import com.example.RealMatch.tag.domain.entity.TagContent;
import com.example.RealMatch.tag.domain.repository.TagContentRepository;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;
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

    public void createCampaignProposal(CustomUserDetails userDetails, CampaignProposalRequestDto request) {
        userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        User creator = userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다."));

        validateRequesterAuthority(userDetails, creator, brand);

        Campaign campaign = null;
        if (request.getCampaignId() != null) {
            campaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(() -> new CustomException(CampaignErrorCode.CAMPAIGN_NOT_FOUND));
        }

        validateDateRange(request);

        CampaignProposal proposal = CampaignProposal.builder()
                .creator(creator)
                .brand(brand)
                .whoProposed(Role.from(userDetails.getRole()))
                .proposedUserId(userDetails.getUserId())
                .campaign(campaign)
                .title(request.getCampaignName())
                .campaignDescription(request.getDescription())
                .rewardAmount(request.getRewardAmount())
                .productId(request.getProductId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        saveAllContentTags(request, proposal);
        campaignProposalRepository.save(proposal);
    }

    @Transactional
    public void modifyCampaignProposal(CustomUserDetails userDetails, UUID campaignProposalId, CampaignProposalRequestDto request) {
        userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        CampaignProposal proposal = campaignProposalRepository.findById(campaignProposalId)
                .orElseThrow(() -> new CustomException(
                        BusinessErrorCode.CAMPAIGN_PROPOSAL_NOT_FOUND
                ));

        validateExistFields(request);
        validateImmutableFields(request, proposal);
        validateModifyAvailable(userDetails, proposal);
        validateDateRange(request);

        proposal.clearContentTags();
        campaignProposalRepository.flush();

        proposal.modify(
                request.getCampaignName(),
                request.getDescription(),
                request.getRewardAmount(),
                request.getProductId(),
                request.getStartDate(),
                request.getEndDate()
        );
        saveAllContentTags(request, proposal);
    }

    private static void validateModifyAvailable(CustomUserDetails userDetails, CampaignProposal proposal) {
        if (!proposal.isModifiable()) {
            throw new CustomException(BusinessErrorCode.CAMPAIGN_PROPOSAL_NOT_MODIFIABLE);
        }

        if (!proposal.getProposedUserId().equals(userDetails.getUserId())) {
            throw new CustomException(BusinessErrorCode.CAMPAIGN_PROPOSAL_FORBIDDEN);
        }

        Role requesterRole = Role.from(userDetails.getRole());
        if (proposal.getWhoProposed() != requesterRole) {
            throw new CustomException(
                    BusinessErrorCode.CAMPAIGN_PROPOSAL_ROLE_MISMATCH
            );
        }
    }


    private void saveAllContentTags(CampaignProposalRequestDto request, CampaignProposal proposal) {
        saveContentTags(proposal, request.getFormats());
        saveContentTags(proposal, request.getCategories());
        saveContentTags(proposal, request.getTones());
        saveContentTags(proposal, request.getInvolvements());
        saveContentTags(proposal, request.getUsageRanges());
    }


    private void saveContentTags(
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

            proposal.addTag(
                    CampaignProposalContentTag.create(
                            proposal,
                            tag,
                            tagRequest.customValue()
                    )
            );
        }
    }

    private void validateExistFields(CampaignProposalRequestDto request) {
        brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다."));

        userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (request.getCampaignId() != null) {
            campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(() -> new CustomException(CampaignErrorCode.CAMPAIGN_NOT_FOUND));
        }
    }

    private static void validateImmutableFields(CampaignProposalRequestDto request, CampaignProposal proposal) {
        if (!proposal.getCreator().getId().equals(request.getCreatorId())) {
            throw new CustomException(BusinessErrorCode.CAMPAIGN_PROPOSAL_CREATOR_IMMUTABLE);
        }

        if (!proposal.getBrand().getId().equals(request.getBrandId())) {
            throw new CustomException(
                    BusinessErrorCode.CAMPAIGN_PROPOSAL_BRAND_IMMUTABLE
            );
        }

        if (proposal.getCampaign() != null) {
            if (!proposal.getCampaign().getId().equals(request.getCampaignId())) {
                throw new CustomException(BusinessErrorCode.CAMPAIGN_PROPOSAL_CAMPAIGN_IMMUTABLE);
            }
        } else {
            if (request.getCampaignId() != null) {
                throw new CustomException(BusinessErrorCode.CAMPAIGN_PROPOSAL_CAMPAIGN_IMMUTABLE);
            }
        }
    }

    private void validateDateRange(CampaignProposalRequestDto request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new CustomException(
                        GeneralErrorCode.INVALID_DATA
                );
            }
        }
    }

    private void validateRequesterAuthority(
            CustomUserDetails userDetails,
            User creator,
            Brand brand
    ) {
        Long requesterId = userDetails.getUserId();

        boolean isCreator = creator.getId().equals(requesterId);
        boolean isBrandOwner = brand.getUser().getId().equals(requesterId);

        if (!isCreator && !isBrandOwner) {
            throw new CustomException(
                    GeneralErrorCode.FORBIDDEN
            );
        }
    }


}
