package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandCategoryView;

public interface BrandCategoryViewRepository extends JpaRepository<BrandCategoryView, Long> {

    List<BrandCategoryView> findByBrandId(Long brandId);

    List<BrandCategoryView> findByCategoryId(Long categoryId);
}
