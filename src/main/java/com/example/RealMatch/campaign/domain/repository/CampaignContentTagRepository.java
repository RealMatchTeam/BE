package com.example.RealMatch.campaign.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.campaign.domain.entity.CampaignContentTag;

public interface CampaignContentTagRepository extends JpaRepository<CampaignContentTag, Long> {

    @Query("""
        select cct
        from CampaignContentTag cct
        join fetch cct.tagContent tc
        where cct.campaign.id = :campaignId
    """)
    List<CampaignContentTag> findAllByCampaignIdWithTag(
            @Param("campaignId") Long campaignId
    );

    void deleteByCampaignId(Long campaignId);
}
