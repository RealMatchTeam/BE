package com.example.RealMatch.match.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchCampaignHistoryEntity;

public interface MatchCampaignHistoryRepository extends JpaRepository<MatchCampaignHistoryEntity, Long> {

    List<MatchCampaignHistoryEntity> findByUserIdAndIsDeletedFalse(Long userId);

    List<MatchCampaignHistoryEntity> findByCampaignIdAndIsDeletedFalse(Long campaignId);

    boolean existsByUserIdAndCampaignIdAndIsDeletedFalse(Long userId, Long campaignId);
}
