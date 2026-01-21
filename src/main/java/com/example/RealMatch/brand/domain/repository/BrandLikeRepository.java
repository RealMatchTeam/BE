package com.example.RealMatch.brand.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandLikeEntity;

public interface BrandLikeRepository extends JpaRepository<BrandLikeEntity, Long> {

    List<BrandLikeEntity> findByUserId(Long userId);

    List<BrandLikeEntity> findByBrandId(Long brandId);

    Optional<BrandLikeEntity> findByUserIdAndBrandId(Long userId, Long brandId);

    boolean existsByUserIdAndBrandId(Long userId, Long brandId);

    void deleteByUserIdAndBrandId(Long userId, Long brandId);

    long countByBrandId(Long brandId);
}
