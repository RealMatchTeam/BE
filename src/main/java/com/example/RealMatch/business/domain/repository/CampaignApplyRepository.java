package com.example.RealMatch.business.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.business.domain.entity.CampaignApply;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.business.presentation.dto.response.CollaborationProjection;

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
    select new com.example.RealMatch.business.presentation.dto.response.CollaborationProjection(
        c.id,
        null,
        b.brandName,
        b.logoUrl,
        c.title,
        ca.applyStatus,
        c.startDate,
        c.endDate,
        com.example.RealMatch.business.domain.enums.CollaborationType.APPLIED
    )
    from CampaignApply ca
    join ca.campaign c
    join c.brand b
    where ca.user.id = :userId
      and (:status is null or ca.applyStatus = :status)
      and (:startDate is null or c.endDate >= :startDate)
      and (:endDate is null or c.startDate <= :endDate)
    """)
    List<CollaborationProjection> findMyAppliedCollaborations(
            Long userId,
            ProposalStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
    select new com.example.RealMatch.business.presentation.dto.response.CollaborationProjection(
        c.id,
        null,
        b.brandName,
        b.logoUrl,
        c.title,
        ca.applyStatus,
        c.startDate,
        c.endDate,
        com.example.RealMatch.business.domain.enums.CollaborationType.APPLIED
    )
    from CampaignApply ca
    join ca.campaign c
    join c.brand b
    where ca.user.id = :userId
      and (:status is null or ca.applyStatus = :status)
      and b.id in :brandIds
    """)
    List<CollaborationProjection> findMyAppliedCollaborationsWithBrand(
            @Param("userId") Long userId,
            @Param("status") ProposalStatus status,
            @Param("brandIds") List<Long> brandIds
    );


}
