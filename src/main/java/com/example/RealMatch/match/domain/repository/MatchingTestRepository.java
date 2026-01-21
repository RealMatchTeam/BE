package com.example.RealMatch.match.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchingTest;

public interface MatchingTestRepository extends JpaRepository<MatchingTest, Long> {

    Optional<MatchingTest> findByUserId(Long userId);
}
