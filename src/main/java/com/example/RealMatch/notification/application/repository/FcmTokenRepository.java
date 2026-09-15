package com.example.RealMatch.notification.application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.RealMatch.notification.domain.entity.FcmToken;

public interface FcmTokenRepository {

    List<FcmToken> findByUserId(Long userId);

    Optional<FcmToken> findByToken(String token);

    int deleteByUserIdAndToken(Long userId,  String token);

    void deleteByUserId(Long userId);

    long countByUserId(Long userId);

    List<FcmToken> findTop10ByUserIdAndLastSeenAtAfterOrderByLastSeenAtDesc(Long userId, LocalDateTime before);

    void upsert(Long userId,  String token,
                 String deviceInfo,  LocalDateTime now);
    void delete(FcmToken entity);

    void deleteByUserIdAndLastSeenAtBefore(Long userId, LocalDateTime before);
}
