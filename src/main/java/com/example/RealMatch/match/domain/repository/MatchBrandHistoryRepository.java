package com.example.RealMatch.match.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.match.domain.entity.MatchBrandHistory;

public interface MatchBrandHistoryRepository extends JpaRepository<MatchBrandHistory, Long> {

    List<MatchBrandHistory> findByUserId(Long userId);

    List<MatchBrandHistory> findByBrandId(Long brandId);

    boolean existsByUserIdAndBrandId(Long userId, Long brandId);

    Optional<MatchBrandHistory> findByUserIdAndBrandId(Long userId, Long brandId);

    Optional<MatchBrandHistory> findByUserIdAndBrandIdAndIsDeprecatedFalse(Long userId, Long brandId);

    List<MatchBrandHistory> findByUserIdOrderByMatchingRatioDesc(Long userId);

    List<MatchBrandHistory> findByUserIdAndIsDeprecatedFalse(Long userId);

    List<MatchBrandHistory> findByUserIdAndIsDeprecatedFalseOrderByMatchingRatioDesc(Long userId);

    @Modifying
    @Query("UPDATE MatchBrandHistory h SET h.isDeprecated = true WHERE h.user.id = :userId AND h.isDeprecated = false")
    int bulkDeprecateByUserId(@Param("userId") Long userId);
}
