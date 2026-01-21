package com.example.RealMatch.campaign.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.CampaignToneEntity;

public interface CampaignToneRepository extends JpaRepository<CampaignToneEntity, Long> {

    List<CampaignToneEntity> findByCampaignIdAndIsDeletedFalse(Long campaignId);
}
