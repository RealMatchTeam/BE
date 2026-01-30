package com.example.RealMatch.business.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.user.domain.entity.enums.Role;

public interface CampaignProposalRepository extends JpaRepository<CampaignProposal, UUID>, CampaignProposalRepositoryCustom {

    @Query("""
    select cp.id
    from CampaignProposal cp
    where
        cp.whoProposed = :role
        and (
            (:role = 'BRAND' and cp.brand.user.id = :userId)
            or
            (:role = 'CREATOR' and cp.creator.id = :userId)
        )
        and (:status is null or cp.status = :status)
    """)
    List<UUID> findSentProposalIds(
            @Param("userId") Long userId,
            @Param("role") Role role,
            @Param("status") ProposalStatus status
    );

    @Query("""
    select cp.id
    from CampaignProposal cp
    where
        cp.whoProposed = :role
        and (
            (:role = 'BRAND' and cp.creator.id = :userId)
            or
            (:role = 'CREATOR' and cp.brand.user.id = :userId)
        )
        and (:status is null or cp.status = :status)
    """)
    List<UUID> findReceivedProposalIds(
            @Param("userId") Long userId,
            @Param("role") Role role,
            @Param("status") ProposalStatus status
    );

    @Query("""
        select distinct cp
        from CampaignProposal cp
        left join fetch cp.tags t
        left join fetch t.tagContent
        where cp.id = :proposalId
    """)
    Optional<CampaignProposal> findByIdWithTags(UUID proposalId);

public interface CampaignProposalRepository extends JpaRepository<CampaignProposal, UUID> {

    List<CampaignProposal> findByCreatorId(Long creatorId);
}
