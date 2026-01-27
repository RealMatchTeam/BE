package com.example.RealMatch.campaign.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.CampaignContentTag;

public interface CampaignContentTagRepository extends JpaRepository<CampaignContentTag, Long> {

    List<CampaignContentTag> findAllByCampaignId(Long campaignId);
}
