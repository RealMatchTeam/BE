package com.example.RealMatch.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.SignupPurpose;

public interface SignupPurposeRepository extends JpaRepository<SignupPurpose, Long> {

    Optional<SignupPurpose> findByPurposeName(String purposeName);
}
