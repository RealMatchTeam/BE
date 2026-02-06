package com.example.RealMatch.campaign.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.campaign.domain.entity.CampaignLike;

public interface CampaignLikeRepository extends JpaRepository<CampaignLike, Long> {

    List<CampaignLike> findByUserId(Long userId);

    List<CampaignLike> findByCampaignId(Long campaignId);

    Optional<CampaignLike> findByUserIdAndCampaignId(Long userId, Long campaignId);

    boolean existsByUserIdAndCampaignId(Long userId, Long campaignId);

    void deleteByUserIdAndCampaignId(Long userId, Long campaignId);

    @Query("""
        select cl.campaign.id
        from CampaignLike cl
        where cl.user.id = :userId
          and cl.campaign.id in :campaignIds
    """)
    Set<Long> findLikedCampaignIds(
            @Param("userId") Long userId,
            @Param("campaignIds") List<Long> campaignIds
    );

    long countByCampaignId(Long campaignId);
}
