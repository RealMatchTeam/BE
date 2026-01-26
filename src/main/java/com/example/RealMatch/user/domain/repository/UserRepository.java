package com.example.RealMatch.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.RealMatch.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // 활성화된(삭제되지 않은) 유저만 조회
    @Query("select u from User u where u.id = :id and u.isDeleted = false")
    Optional<User> findById(Long id);

    // 이메일로 조회 (Spring Security 인증 시 주로 사용)
    @Query("select u from User u where u.email = :email and u.isDeleted = false")
    Optional<User> findByEmail(String email);
}
