package com.example.RealMatch.campaign.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.campaign.domain.entity.Campaign;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findById(Long id);

    List<Campaign> findAll();

    List<Campaign> findByCreatedBy(Long createdBy);

    List<Campaign> findByRecruitEndDateAfter(LocalDateTime now);

    @Query("""
        SELECT c
        FROM Campaign c
        WHERE c.brand.id = :brandId
          AND (:cursor IS NULL OR c.id < :cursor)
        ORDER BY
          CASE
            WHEN CURRENT_TIMESTAMP > c.recruitEndDate THEN c.recruitEndDate
            ELSE c.recruitStartDate
          END DESC,
          c.id DESC
    """)
    List<Campaign> findBrandCampaignsWithCursor(
            @Param("brandId") Long brandId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
