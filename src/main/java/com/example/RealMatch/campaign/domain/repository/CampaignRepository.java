package com.example.RealMatch.campaign.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.CampaignEntity;

public interface CampaignRepository extends JpaRepository<CampaignEntity, Long> {

    Optional<CampaignEntity> findByIdAndIsDeletedFalse(Long id);

    List<CampaignEntity> findByIsDeletedFalse();

    List<CampaignEntity> findByCreatedByAndIsDeletedFalse(Long createdBy);

    List<CampaignEntity> findByRecruitEndDateAfterAndIsDeletedFalse(LocalDateTime now);

    List<CampaignEntity> findByTitleContainingAndIsDeletedFalse(String title);
}
