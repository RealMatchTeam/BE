package com.example.RealMatch.business.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.business.domain.entity.CampaignApply;
import com.example.RealMatch.business.domain.enums.ProposalStatus;

public interface CampaignApplyRepository extends JpaRepository<CampaignApply, Long> {

    @Query("""
            SELECT ca.campaign.id, COUNT(ca)
            FROM CampaignApply ca
            WHERE ca.campaign.id IN :campaignIds
            GROUP BY ca.campaign.id
            """)
    List<Object[]> countByCampaignIdIn(@Param("campaignIds") List<Long> campaignIds);
    boolean existsByUserIdAndCampaignId(Long userId, Long campaignId);
    Optional<CampaignApply> findByCampaignIdAndUserId(Long campaignId, Long userId);

    @Query("""
    select ca
    from CampaignApply ca
    join fetch ca.campaign c
    join fetch c.brand b
    where ca.user.id = :userId
      and (:status is null or ca.proposalStatus = :status)
      and (:startDate is null or c.startDate >= :startDate)
      and (:endDate is null or c.endDate <= :endDate)
""")
    List<CampaignApply> findMyApplies(
            @Param("userId") Long userId,
            @Param("status") ProposalStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
