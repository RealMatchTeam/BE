package com.example.RealMatch.brand.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandLikeReadEntity;

public interface BrandLikeReadRepository extends JpaRepository<BrandLikeReadEntity, Long> {

    Optional<BrandLikeReadEntity> findByBrandIdAndIsDeletedFalse(Long brandId);
}
