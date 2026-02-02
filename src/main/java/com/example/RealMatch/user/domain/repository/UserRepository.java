package com.example.RealMatch.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // 활성화된(삭제되지 않은) 유저만 조회
    @Query("select u from User u where u.id = :id and u.isDeleted = false")
    Optional<User> findById(@Param("id") Long id);

    boolean existsByNickname(String nickname);
}
