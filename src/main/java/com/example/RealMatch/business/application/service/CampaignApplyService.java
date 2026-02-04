package com.example.RealMatch.business.application.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.business.application.event.CampaignApplySentEvent;
import com.example.RealMatch.business.application.event.CampaignApplyStatusChangedEvent;
import com.example.RealMatch.business.domain.entity.CampaignApply;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
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

    private static final int CAMPAIGN_DESCRIPTION_MAX_LENGTH = 100;

    private final CampaignApplyRepository campaignApplyRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        // 지원 이벤트 발행
        publishApplySentEvent(campaignApply, campaign);
    }

    private void publishApplySentEvent(CampaignApply apply, Campaign campaign) {
        Long creatorUserId = apply.getUser().getId();
        Long brandUserId = campaign.getBrand().getUser().getId();

        String campaignDescription = campaign.getDescription();
        if (campaignDescription != null && campaignDescription.length() > CAMPAIGN_DESCRIPTION_MAX_LENGTH) {
            campaignDescription = campaignDescription.substring(0, CAMPAIGN_DESCRIPTION_MAX_LENGTH) + "...";
        }

        CampaignApplySentEvent event = new CampaignApplySentEvent(
                apply.getId(),
                campaign.getId(),
                creatorUserId,
                brandUserId,
                campaign.getTitle(),
                campaignDescription,
                apply.getReason()
        );
        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void cancelCampaignApply(Long campaignApplyId, Long userId) {
        CampaignApply campaignApply = campaignApplyRepository.findById(campaignApplyId)
                .orElseThrow(() ->
                        new CustomException(BusinessErrorCode.CAMPAIGN_APPLY_NOT_FOUND)
                );

        if (!campaignApply.getUser().getId().equals(userId)) {
            throw new CustomException(BusinessErrorCode.CAMPAIGN_APPLY_FORBIDDEN);
        }

        if (!campaignApply.isCancelable()) {
            throw new CustomException(BusinessErrorCode.CAMPAIGN_APPLY_NOT_CANCELABLE);
        }

        campaignApply.cancel();

        // 상태 변경 이벤트 발행 (취소한 유저 = creatorUserId)
        Campaign campaign = campaignApply.getCampaign();
        publishApplyStatusChangedEvent(campaignApply, campaign, ProposalStatus.CANCELED, userId);
    }

    // TODO: 지원 수락 기능 구현 필요 (데모데이 이후)
    // 브랜드가 크리에이터의 지원을 수락하는 API

    // TODO: 지원 거절 기능 구현 필요 (데모데이 이후)
    // 브랜드가 크리에이터의 지원을 거절하는 API

    private void publishApplyStatusChangedEvent(
            CampaignApply apply,
            Campaign campaign,
            ProposalStatus newStatus,
            Long actorUserId
    ) {
        Long creatorUserId = apply.getUser().getId();
        Long brandUserId = campaign.getBrand().getUser().getId();

        CampaignApplyStatusChangedEvent event = new CampaignApplyStatusChangedEvent(
                apply.getId(),
                campaign.getId(),
                creatorUserId,
                brandUserId,
                newStatus,
                actorUserId
        );
        eventPublisher.publishEvent(event);
    }

}
