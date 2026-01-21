package com.example.RealMatch.brand.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandCategory;

public interface BrandCategoryRepository extends JpaRepository<BrandCategory, Long> {

    Optional<BrandCategory> findById(Long id);

}
