package com.example.RealMatch.brand.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandLikeRead;

public interface BrandLikeReadRepository extends JpaRepository<BrandLikeRead, Long> {

    Optional<BrandLikeRead> findByBrandId(Long brandId);
}
