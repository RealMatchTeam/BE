package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandImageEntity;

public interface BrandImageRepository extends JpaRepository<BrandImageEntity, Long> {

    List<BrandImageEntity> findByBrandIdAndIsDeletedFalse(Long brandId);
}
