package com.example.RealMatch.match.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchCampaignRatioEntity;

public interface MatchCampaignRatioRepository extends JpaRepository<MatchCampaignRatioEntity, Long> {

    List<MatchCampaignRatioEntity> findByUserIdAndIsDeletedFalse(Long userId);

    List<MatchCampaignRatioEntity> findByCampaignIdAndIsDeletedFalse(Long campaignId);

    Optional<MatchCampaignRatioEntity> findByUserIdAndCampaignIdAndIsDeletedFalse(Long userId, Long campaignId);

    List<MatchCampaignRatioEntity> findByUserIdAndIsDeletedFalseOrderByRatioDesc(Long userId);
}
