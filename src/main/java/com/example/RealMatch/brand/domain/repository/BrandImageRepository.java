package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandImage;

public interface BrandImageRepository extends JpaRepository<BrandImage, Long> {

    List<BrandImage> findAllByBrandId(Long brandId);
}
