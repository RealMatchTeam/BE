package com.example.RealMatch.campaign.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.CampaignProposalEntity;
import com.example.RealMatch.campaign.domain.entity.enums.ProposalStatus;

public interface CampaignProposalRepository extends JpaRepository<CampaignProposalEntity, Long> {

    List<CampaignProposalEntity> findByUserId(Long userId);

    List<CampaignProposalEntity> findByCampaignId(Long campaignId);

    List<CampaignProposalEntity> findByUserIdAndStatus(Long userId, ProposalStatus status);

    List<CampaignProposalEntity> findByCampaignIdAndStatus(Long campaignId, ProposalStatus status);

    Optional<CampaignProposalEntity> findByUserIdAndCampaignId(Long userId, Long campaignId);
}
