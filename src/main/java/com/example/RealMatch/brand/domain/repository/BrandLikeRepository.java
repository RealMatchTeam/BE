package com.example.RealMatch.brand.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandLike;

public interface BrandLikeRepository extends JpaRepository<BrandLike, Long> {

    List<BrandLike> findByUserId(Long userId);

    List<BrandLike> findByBrandId(Long brandId);

    Optional<BrandLike> findByUserIdAndBrandId(Long userId, Long brandId);

    boolean existsByUserIdAndBrandId(Long userId, Long brandId);

    void deleteByUserIdAndBrandId(Long userId, Long brandId);

    long countByBrandId(Long brandId);
}
