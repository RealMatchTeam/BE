package com.example.RealMatch.campaign.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.entity.CampaignLike;
import com.example.RealMatch.campaign.domain.repository.CampaignLikeRepository;
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
public class CampaignLikeService {

    private final CampaignLikeRepository campaignLikeRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    public boolean toggleLike(Long userId, Long campaignId) {
        if (campaignLikeRepository.existsByUserIdAndCampaignId(userId, campaignId)) {
            campaignLikeRepository.deleteByUserIdAndCampaignId(userId, campaignId);
            return false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new CustomException(CampaignErrorCode.CAMPAIGN_NOT_FOUND));

        campaignLikeRepository.save(
                CampaignLike.builder()
                        .user(user)
                        .campaign(campaign)
                        .build()
        );
        return true;
    }
}
