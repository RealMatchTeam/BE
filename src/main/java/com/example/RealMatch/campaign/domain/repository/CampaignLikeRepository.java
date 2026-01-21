package com.example.RealMatch.campaign.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.CampaignLikeEntity;

public interface CampaignLikeRepository extends JpaRepository<CampaignLikeEntity, Long> {

    List<CampaignLikeEntity> findByUserId(Long userId);

    List<CampaignLikeEntity> findByCampaignId(Long campaignId);

    Optional<CampaignLikeEntity> findByUserIdAndCampaignId(Long userId, Long campaignId);

    boolean existsByUserIdAndCampaignId(Long userId, Long campaignId);

    void deleteByUserIdAndCampaignId(Long userId, Long campaignId);

    long countByCampaignId(Long campaignId);
}
