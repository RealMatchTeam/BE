package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;

public interface BrandAvailableSponsorRepository extends JpaRepository<BrandAvailableSponsor, Long> {

    List<BrandAvailableSponsor> findByBrandId(Long brandId);

    List<BrandAvailableSponsor> findByCampaignId(Long campaignId);
}
