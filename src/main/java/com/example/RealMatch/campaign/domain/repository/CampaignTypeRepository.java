package com.example.RealMatch.campaign.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.CampaignTypeEntity;

public interface CampaignTypeRepository extends JpaRepository<CampaignTypeEntity, Long> {

    List<CampaignTypeEntity> findByCampaignIdAndIsDeletedFalse(Long campaignId);
}
