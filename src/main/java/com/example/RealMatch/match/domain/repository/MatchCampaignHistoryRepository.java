package com.example.RealMatch.match.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.match.domain.entity.MatchCampaignHistory;

public interface MatchCampaignHistoryRepository extends JpaRepository<MatchCampaignHistory, Long>, MatchCampaignHistoryRepositoryCustom {

    List<MatchCampaignHistory> findByUserId(Long userId);

    List<MatchCampaignHistory> findByCampaignId(Long campaignId);

    boolean existsByUserIdAndCampaignId(Long userId, Long campaignId);

    boolean existsByUserId(Long userId);

    Optional<MatchCampaignHistory> findByUserIdAndCampaignId(Long userId, Long campaignId);

    List<MatchCampaignHistory> findByUserIdOrderByMatchingRatioDesc(Long userId);

    List<MatchCampaignHistory> findByUserIdAndIsDeprecatedFalse(Long userId);

    List<MatchCampaignHistory> findByUserIdAndIsDeprecatedFalseOrderByMatchingRatioDesc(Long userId);

    @Modifying
    @Query("UPDATE MatchCampaignHistory h SET h.isDeprecated = true WHERE h.user.id = :userId AND h.isDeprecated = false")
    int bulkDeprecateByUserId(@Param("userId") Long userId);
}

