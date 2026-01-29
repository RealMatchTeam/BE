package com.example.RealMatch.business.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.business.domain.entity.CampaignProposalTag;

public interface CampaignProposalTagRepository extends JpaRepository<CampaignProposalTag, Long> {

    @Query("""
        select cpt
        from CampaignProposalTag cpt
        join fetch cpt.tag t
        where cpt.campaignProposal.id = :proposalId
    """)
    List<CampaignProposalTag> findAllByProposalIdWithTag(@Param("proposalId") UUID proposalId);

    void deleteByCampaignProposalId(UUID proposalId);
}
