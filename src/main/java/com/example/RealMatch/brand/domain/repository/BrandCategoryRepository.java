package com.example.RealMatch.brand.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandCategoryEntity;

public interface BrandCategoryRepository extends JpaRepository<BrandCategoryEntity, Long> {

    Optional<BrandCategoryEntity> findByIdAndIsDeletedFalse(Long id);

    List<BrandCategoryEntity> findByIsDeletedFalse();

    Optional<BrandCategoryEntity> findByNameAndIsDeletedFalse(String name);
}
