package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandCategoryViewEntity;

public interface BrandCategoryViewRepository extends JpaRepository<BrandCategoryViewEntity, Long> {

    List<BrandCategoryViewEntity> findByBrandIdAndIsDeletedFalse(Long brandId);

    List<BrandCategoryViewEntity> findByCategoryIdAndIsDeletedFalse(Long categoryId);
}
