package com.example.RealMatch.business.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.business.domain.entity.CampaignApply;
import com.example.RealMatch.business.domain.repository.CampaignApplyRepository;
import com.example.RealMatch.business.exception.BusinessErrorCode;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.campaign.exception.CampaignErrorCode;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.presentation.code.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignApplyService {

    private final CampaignApplyRepository campaignApplyRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    public void applyCampaign(Long campaignId, Long userId, String reason) {

        // 1️⃣ 중복 지원 체크 (가장 먼저)
        if (campaignApplyRepository.existsByUserIdAndCampaignId(userId, campaignId)) {
            throw new CustomException(BusinessErrorCode.CAMPAIGN_APPLY_ALREADY_APPLIED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new CustomException(CampaignErrorCode.CAMPAIGN_NOT_FOUND));

        CampaignApply campaignApply = CampaignApply.builder()
                .user(user)
                .campaign(campaign)
                .reason(reason)
                .build();

        campaignApplyRepository.save(campaignApply);
    }
}
