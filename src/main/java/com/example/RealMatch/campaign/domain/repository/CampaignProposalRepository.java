package com.example.RealMatch.campaign.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.enums.ProposalStatus;

public interface CampaignProposalRepository extends JpaRepository<CampaignProposal, Long> {

    List<CampaignProposal> findByUserId(Long userId);

    List<CampaignProposal> findByCampaignId(Long campaignId);

    List<CampaignProposal> findByUserIdAndStatus(Long userId, ProposalStatus status);

    List<CampaignProposal> findByCampaignIdAndStatus(Long campaignId, ProposalStatus status);

    Optional<CampaignProposal> findByUserIdAndCampaignId(Long userId, Long campaignId);
}
