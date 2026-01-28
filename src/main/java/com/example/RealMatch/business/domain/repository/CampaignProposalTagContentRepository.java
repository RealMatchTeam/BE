package com.example.RealMatch.business.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.business.domain.entity.CampaignProposalContentTag;

public interface CampaignProposalTagContentRepository extends JpaRepository<CampaignProposalContentTag, UUID> {
}
