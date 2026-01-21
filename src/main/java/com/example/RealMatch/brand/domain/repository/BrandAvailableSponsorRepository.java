package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsorEntity;

public interface BrandAvailableSponsorRepository extends JpaRepository<BrandAvailableSponsorEntity, Long> {

    List<BrandAvailableSponsorEntity> findByBrandIdAndIsDeletedFalse(Long brandId);

    List<BrandAvailableSponsorEntity> findByCampaignIdAndIsDeletedFalse(Long campaignId);
}
