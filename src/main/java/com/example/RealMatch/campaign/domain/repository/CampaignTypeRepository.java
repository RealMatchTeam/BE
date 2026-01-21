package com.example.RealMatch.campaign.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.CampaignType;

public interface CampaignTypeRepository extends JpaRepository<CampaignType, Long> {

    List<CampaignType> findByCampaignId(Long campaignId);
}
