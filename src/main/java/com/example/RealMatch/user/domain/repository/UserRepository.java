package com.example.RealMatch.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.RealMatch.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // 활성화된(삭제되지 않은) 유저만 조회
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findById(Long id);

    boolean existsByNickname(String nickname);
}
