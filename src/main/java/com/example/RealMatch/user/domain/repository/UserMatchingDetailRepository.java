package com.example.RealMatch.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.UserMatchingDetail;

public interface UserMatchingDetailRepository extends JpaRepository<UserMatchingDetail, Long> {
    Optional<UserMatchingDetail> findByUserId(Long userId);
}
