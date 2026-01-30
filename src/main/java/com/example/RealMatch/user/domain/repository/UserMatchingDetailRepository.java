package com.example.RealMatch.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.UserMatchingDetail;

public interface UserMatchingDetailRepository extends JpaRepository<UserMatchingDetail, UUID> {
    Optional<UserMatchingDetail> findByUserId(Long userId);
}
