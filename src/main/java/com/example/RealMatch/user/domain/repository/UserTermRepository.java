package com.example.RealMatch.user.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.UserTermEntity;

public interface UserTermRepository extends JpaRepository<UserTermEntity, Long> {

    List<UserTermEntity> findByUserId(Long userId);

    Optional<UserTermEntity> findByUserIdAndTermId(Long userId, Long termId);

    List<UserTermEntity> findByUserIdAndIsAgreed(Long userId, boolean isAgreed);
}
