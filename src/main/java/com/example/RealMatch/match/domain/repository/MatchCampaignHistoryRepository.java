package com.example.RealMatch.match.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchCampaignHistory;

public interface MatchCampaignHistoryRepository extends JpaRepository<MatchCampaignHistory, Long> {

    List<MatchCampaignHistory> findByUserId(Long userId);

    List<MatchCampaignHistory> findByCampaignId(Long campaignId);

    boolean existsByUserIdAndCampaignId(Long userId, Long campaignId);

    boolean existsByUserId(Long userId);
}

