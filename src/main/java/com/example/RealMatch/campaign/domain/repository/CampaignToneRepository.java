package com.example.RealMatch.campaign.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.CampaignTone;

public interface CampaignToneRepository extends JpaRepository<CampaignTone, Long> {

    List<CampaignTone> findByCampaignId(Long campaignId);
}
