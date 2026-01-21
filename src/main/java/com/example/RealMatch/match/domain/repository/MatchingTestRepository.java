package com.example.RealMatch.match.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchingTestEntity;

public interface MatchingTestRepository extends JpaRepository<MatchingTestEntity, Long> {

    Optional<MatchingTestEntity> findByUserId(Long userId);
}
