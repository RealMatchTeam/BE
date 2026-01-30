package com.example.RealMatch.campaign.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.entity.CampaignContentTag;
import com.example.RealMatch.campaign.domain.repository.CampaignContentTagRepository;
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

    public CampaignDetailResponse getCampaignDetail(Long campaignId) {

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() ->
                        new CustomException(CampaignErrorCode.CAMPAIGN_NOT_FOUND)
                );

        List<CampaignContentTag> tags = campaignContentTagRepository.findAllByCampaignIdWithTag(campaignId);

        return CampaignDetailResponse.from(campaign, tags);
    }
}
