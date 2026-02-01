package com.example.RealMatch.match.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchBrandHistory;

public interface MatchBrandHistoryRepository extends JpaRepository<MatchBrandHistory, Long> {

    List<MatchBrandHistory> findByUserId(Long userId);

    List<MatchBrandHistory> findByBrandId(Long brandId);

    boolean existsByUserIdAndBrandId(Long userId, Long brandId);

    Optional<MatchBrandHistory> findByUserIdAndBrandId(Long userId, Long brandId);

    List<MatchBrandHistory> findByUserIdOrderByMatchingRatioDesc(Long userId);
}
