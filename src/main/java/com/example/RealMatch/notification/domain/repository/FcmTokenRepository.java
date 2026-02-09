package com.example.RealMatch.notification.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.notification.domain.entity.FcmToken;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    List<FcmToken> findByUserId(Long userId);

    Optional<FcmToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUserId(Long userId);
}
