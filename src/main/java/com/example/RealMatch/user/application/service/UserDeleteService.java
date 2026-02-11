package com.example.RealMatch.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.repository.BrandLikeRepository;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.business.domain.repository.CampaignApplyRepository;
import com.example.RealMatch.business.domain.repository.CampaignProposalRepository;
import com.example.RealMatch.campaign.domain.repository.CampaignLikeRepository;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.match.domain.repository.MatchBrandHistoryRepository;
import com.example.RealMatch.match.domain.repository.MatchCampaignHistoryRepository;
import com.example.RealMatch.tag.domain.repository.TagUserRepository;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.AuthenticationMethodRepository;
import com.example.RealMatch.user.domain.repository.NotificationSettingRepository;
import com.example.RealMatch.user.domain.repository.UserContentCategoryRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.domain.repository.UserSignupPurposeRepository;
import com.example.RealMatch.user.domain.repository.UserTermRepository;
import com.example.RealMatch.user.presentation.code.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDeleteService {

    private final UserRepository userRepository;
    private final AuthenticationMethodRepository authenticationMethodRepository;
    private final BrandRepository brandRepository;
    private final BrandLikeRepository brandLikeRepository;
    private final CampaignApplyRepository campaignApplyRepository;
    private final CampaignLikeRepository campaignLikeRepository;
    private final CampaignProposalRepository campaignProposalRepository;
    private final MatchBrandHistoryRepository matchBrandHistoryRepository;
    private final MatchCampaignHistoryRepository matchCampaignHistoryRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final TagUserRepository tagUserRepository;
    private final UserContentCategoryRepository userContentCategoryRepository;
    private final UserSignupPurposeRepository userSignupPurposeRepository;
    private final UserTermRepository userTermRepository;

    @Transactional
    public void deleteUserImmediately(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        deleteRelatedData(userId);
        userRepository.delete(user);
    }

    private void deleteRelatedData(Long userId) {
        tagUserRepository.deleteByUserId(userId);
        userContentCategoryRepository.deleteByUserId(userId);
        userSignupPurposeRepository.deleteByUserId(userId);
        userTermRepository.deleteByUserId(userId);
        notificationSettingRepository.deleteByUserId(userId);
        matchBrandHistoryRepository.deleteByUserId(userId);
        matchCampaignHistoryRepository.deleteByUserId(userId);
        brandLikeRepository.deleteByUserId(userId);
        campaignLikeRepository.deleteByUserId(userId);
        campaignApplyRepository.deleteByUserId(userId);
        campaignProposalRepository.deleteByUserId(userId);
        brandRepository.deleteByUserId(userId);
        authenticationMethodRepository.deleteByUserId(userId);
    }
}
