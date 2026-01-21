package com.example.RealMatch.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.SignupPurposeEntity;

public interface SignupPurposeRepository extends JpaRepository<SignupPurposeEntity, Long> {

    Optional<SignupPurposeEntity> findByPurposeName(String purposeName);
}
