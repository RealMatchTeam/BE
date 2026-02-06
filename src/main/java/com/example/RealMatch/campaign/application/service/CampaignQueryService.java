package com.example.RealMatch.campaign.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.service.AttachmentUrlService;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.entity.CampaignContentTag;
import com.example.RealMatch.campaign.domain.repository.CampaignContentTagRepository;
import com.example.RealMatch.campaign.domain.repository.CampaignLikeRepository;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.campaign.exception.CampaignErrorCode;
import com.example.RealMatch.campaign.presentation.dto.response.CampaignDetailResponse;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignQueryService {

    private final CampaignRepository campaignRepository;
    private final CampaignContentTagRepository campaignContentTagRepository;
    private final CampaignLikeRepository campaignLikeRepository;
    private final AttachmentUrlService attachmentUrlService;

    public CampaignDetailResponse getCampaignDetail(Long userId, Long campaignId) {

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() ->
                        new CustomException(CampaignErrorCode.CAMPAIGN_NOT_FOUND)
                );

        boolean isLike = campaignLikeRepository.existsByUserIdAndCampaignId(userId, campaignId);

        List<CampaignContentTag> tags = campaignContentTagRepository.findAllByCampaignIdWithTag(campaignId);

        String imageUrl = attachmentUrlService.getAccessUrl(campaign.getImageUrl());

        return CampaignDetailResponse.from(campaign, imageUrl, isLike, tags);
    }
}
