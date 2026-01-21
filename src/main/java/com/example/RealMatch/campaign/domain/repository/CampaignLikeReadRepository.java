package com.example.RealMatch.campaign.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.CampaignLikeRead;

public interface CampaignLikeReadRepository extends JpaRepository<CampaignLikeRead, Long> {

    Optional<CampaignLikeRead> findByCampaignId(Long campaignId);
}
