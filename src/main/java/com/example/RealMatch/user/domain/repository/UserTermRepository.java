package com.example.RealMatch.user.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.UserTerm;

public interface UserTermRepository extends JpaRepository<UserTerm, UUID> {

    List<UserTerm> findByUserId(Long userId);

    Optional<UserTerm> findByUserIdAndTermId(Long userId, Long termId);

    List<UserTerm> findByUserIdAndIsAgreed(Long userId, boolean isAgreed);
}
