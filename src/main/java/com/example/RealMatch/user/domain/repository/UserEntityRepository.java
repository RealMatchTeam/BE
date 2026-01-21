package com.example.RealMatch.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.User;

public interface UserEntityRepository extends JpaRepository<User, Long> {

//    Optional<User> findByEmail(String email);
//
//    Optional<User> findByNickname(String nickname);
//
//    boolean existsByEmail(String email);
//
//    boolean existsByNickname(String nickname);
}
