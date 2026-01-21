package com.example.RealMatch.user.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.UserSignupPurposeEntity;

public interface UserSignupPurposeRepository extends JpaRepository<UserSignupPurposeEntity, Long> {

    List<UserSignupPurposeEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
