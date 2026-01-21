package com.example.RealMatch.user.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.UserSignupPurpose;

public interface UserSignupPurposeRepository extends JpaRepository<UserSignupPurpose, Long> {

    List<UserSignupPurpose> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
