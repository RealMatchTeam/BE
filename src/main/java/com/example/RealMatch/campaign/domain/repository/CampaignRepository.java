package com.example.RealMatch.campaign.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.campaign.domain.entity.Campaign;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findById(Long id);

    List<Campaign> findAll();

    List<Campaign> findByCreatedBy(Long createdBy);

    List<Campaign> findByRecruitEndDateAfter(LocalDateTime now);

    List<Campaign> findByTitleContaining(String title);
}
