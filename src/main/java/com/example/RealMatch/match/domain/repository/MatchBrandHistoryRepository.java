package com.example.RealMatch.match.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchBrandHistoryEntity;

public interface MatchBrandHistoryRepository extends JpaRepository<MatchBrandHistoryEntity, Long> {

    List<MatchBrandHistoryEntity> findByUserIdAndIsDeletedFalse(Long userId);

    List<MatchBrandHistoryEntity> findByBrandIdAndIsDeletedFalse(Long brandId);

    boolean existsByUserIdAndBrandIdAndIsDeletedFalse(Long userId, Long brandId);
}
