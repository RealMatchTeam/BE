package com.example.RealMatch.match.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchCampaignRatio;

public interface MatchCampaignRatioRepository extends JpaRepository<MatchCampaignRatio, Long> {

    List<MatchCampaignRatio> findByUserId(Long userId);

    List<MatchCampaignRatio> findByCampaignId(Long campaignId);

    Optional<MatchCampaignRatio> findByUserIdAndCampaignId(Long userId, Long campaignId);

    List<MatchCampaignRatio> findByUserIdOrderByRatioDesc(Long userId);
}
