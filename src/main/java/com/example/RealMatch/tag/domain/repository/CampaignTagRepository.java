package com.example.RealMatch.tag.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.tag.domain.entity.CampaignTag;

public interface CampaignTagRepository extends JpaRepository<CampaignTag, Long> {

    @Query("""
        select ct
        from CampaignTag ct
        join fetch ct.tag t
        where ct.campaign.id = :campaignId
    """)
    List<CampaignTag> findAllByCampaignIdWithTag(@Param("campaignId") Long campaignId);

    List<CampaignTag> findAllByCampaignId(Long campaignId);

    void deleteByCampaignId(Long campaignId);
}
