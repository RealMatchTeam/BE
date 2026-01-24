package com.example.RealMatch.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // 활성화된(삭제되지 않은) 유저만 조회
    Optional<User> findByIdAndIsDeletedFalse(Long id);

    // 이메일로 조회 (Spring Security 인증 시 주로 사용)
    Optional<User> findByEmail(String email);
}
