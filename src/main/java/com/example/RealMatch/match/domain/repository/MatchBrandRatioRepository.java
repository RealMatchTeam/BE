package com.example.RealMatch.match.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchBrandRatio;

public interface MatchBrandRatioRepository extends JpaRepository<MatchBrandRatio, Long> {

    List<MatchBrandRatio> findByUserId(Long userId);

    List<MatchBrandRatio> findByBrandId(Long brandId);

    Optional<MatchBrandRatio> findByUserIdAndBrandId(Long userId, Long brandId);

    List<MatchBrandRatio> findByUserIdOrderByRatioDesc(Long userId);
}
