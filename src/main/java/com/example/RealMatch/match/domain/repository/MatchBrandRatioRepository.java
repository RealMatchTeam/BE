package com.example.RealMatch.match.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchBrandRatioEntity;

public interface MatchBrandRatioRepository extends JpaRepository<MatchBrandRatioEntity, Long> {

    List<MatchBrandRatioEntity> findByUserIdAndIsDeletedFalse(Long userId);

    List<MatchBrandRatioEntity> findByBrandIdAndIsDeletedFalse(Long brandId);

    Optional<MatchBrandRatioEntity> findByUserIdAndBrandIdAndIsDeletedFalse(Long userId, Long brandId);

    List<MatchBrandRatioEntity> findByUserIdAndIsDeletedFalseOrderByRatioDesc(Long userId);
}
