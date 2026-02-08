package com.example.RealMatch.business.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.enums.ProposalStatus;

public interface CampaignProposalRepository extends JpaRepository<CampaignProposal, Long>, CampaignProposalRepositoryCustom {

    @Query("""
    select cp.id
    from CampaignProposal cp
    where cp.senderUserId= :userId
      and (:status is null or cp.status = :status)
    and (:startDate is null or cp.endDate >= :startDate)
    and (:endDate is null or cp.startDate <= :endDate)
""")
    List<Long> findSentProposalIds(
            @Param("userId") Long userId,
            @Param("status") ProposalStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    select cp.id
    from CampaignProposal cp
    where cp.receiverUserId = :userId
      and (:status is null or cp.status = :status)
      and (:startDate is null or cp.endDate >= :startDate)
      and (:endDate is null or cp.startDate <= :endDate)
""")
    List<Long> findReceivedProposalIds(
            @Param("userId") Long userId,
            @Param("status") ProposalStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        select distinct cp
        from CampaignProposal cp
        left join fetch cp.tags t
        left join fetch t.tagContent
        where cp.id = :proposalId
    """)
    Optional<CampaignProposal> findByIdWithTags(Long proposalId);

    @Query("SELECT p FROM CampaignProposal p " +
            "LEFT JOIN FETCH p.campaign c " +
            "LEFT JOIN FETCH c.brand " +
            "LEFT JOIN FETCH p.brand " +
            "WHERE p.id IN :ids")
    List<CampaignProposal> findAllByIdWithDetails(@Param("ids") List<Long> ids);

}
