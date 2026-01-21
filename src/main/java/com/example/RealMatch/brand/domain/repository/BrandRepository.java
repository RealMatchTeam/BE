package com.example.RealMatch.brand.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandEntity;
import com.example.RealMatch.brand.domain.entity.enums.IndustryType;

public interface BrandRepository extends JpaRepository<BrandEntity, Long> {

    Optional<BrandEntity> findByIdAndIsDeletedFalse(Long id);

    List<BrandEntity> findByIsDeletedFalse();

    List<BrandEntity> findByIndustryTypeAndIsDeletedFalse(IndustryType industryType);

    List<BrandEntity> findByBrandNameContainingAndIsDeletedFalse(String brandName);
}
