package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;

public interface BrandAvailableSponsorRepository extends JpaRepository<BrandAvailableSponsor, Long> {

    List<BrandAvailableSponsor> findByBrandId(Long brandId);

    @Query("SELECT s FROM BrandAvailableSponsor s LEFT JOIN FETCH s.images WHERE s.brand.id = :brandId")
    List<BrandAvailableSponsor> findByBrandIdWithImages(@Param("brandId") Long brandId);

    List<BrandAvailableSponsor> findByCampaignId(Long campaignId);
}
