package com.example.RealMatch.business.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.business.domain.entity.CampaignProposal;

public interface CampaignProposalRepository extends JpaRepository<CampaignProposal, UUID> {

    List<CampaignProposal> findByCreatorId(Long creatorId);
}
