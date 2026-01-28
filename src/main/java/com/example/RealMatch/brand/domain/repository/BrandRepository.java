package com.example.RealMatch.brand.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.enums.IndustryType;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findById(Long id);

    List<Brand> findAll();

    List<Brand> findByIndustryType(IndustryType industryType);

    List<Brand> findByBrandNameContaining(String brandName);

    Optional<Brand> findByCreatedBy(Long createdBy);

    List<Brand> findByCreatedByIn(List<Long> createdByList);
}
