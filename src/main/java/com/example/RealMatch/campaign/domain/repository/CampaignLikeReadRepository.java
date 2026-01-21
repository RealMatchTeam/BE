package com.example.RealMatch.campaign.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.CampaignLikeReadEntity;

public interface CampaignLikeReadRepository extends JpaRepository<CampaignLikeReadEntity, Long> {

    Optional<CampaignLikeReadEntity> findByCampaignIdAndIsDeletedFalse(Long campaignId);
}
