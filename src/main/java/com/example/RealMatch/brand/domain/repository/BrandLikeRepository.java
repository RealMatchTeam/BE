package com.example.RealMatch.brand.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.BrandLike;
import com.example.RealMatch.user.domain.entity.User;

public interface BrandLikeRepository extends JpaRepository<BrandLike, Long> {

    List<BrandLike> findByUserId(Long userId);

    List<BrandLike> findByBrandId(Long brandId);

    Optional<BrandLike> findByUserIdAndBrandId(Long userId, Long brandId);

    Optional<BrandLike> findByUserAndBrand(User user, Brand brand);

    boolean existsByUserIdAndBrandId(Long userId, Long brandId);

    void deleteByUserIdAndBrandId(Long userId, Long brandId);

    long countByBrandId(Long brandId);
}
