package com.example.RealMatch.campaign.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.business.domain.entity.CampaignApply;

public interface CampaignApplyRepository extends JpaRepository<CampaignApply, Long> {

}
