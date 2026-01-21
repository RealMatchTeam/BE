package com.example.RealMatch.campaign.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.CampaignApplyEntity;

public interface CampaignApplyRepository extends JpaRepository<CampaignApplyEntity, Long> {

    List<CampaignApplyEntity> findByUserIdAndIsDeletedFalse(Long userId);

    List<CampaignApplyEntity> findByCampaignIdAndIsDeletedFalse(Long campaignId);

    Optional<CampaignApplyEntity> findByUserIdAndCampaignIdAndIsDeletedFalse(Long userId, Long campaignId);

    boolean existsByUserIdAndCampaignIdAndIsDeletedFalse(Long userId, Long campaignId);
}
