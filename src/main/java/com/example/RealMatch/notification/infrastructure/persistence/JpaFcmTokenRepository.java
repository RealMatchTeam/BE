package com.example.RealMatch.notification.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.notification.application.repository.FcmTokenRepository;
import com.example.RealMatch.notification.domain.entity.FcmToken;

public interface JpaFcmTokenRepository extends JpaRepository<FcmToken, Long>, FcmTokenRepository {

    List<FcmToken> findByUserId(Long userId);

    Optional<FcmToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM FcmToken t WHERE t.userId = :userId AND t.token = :token")
    int deleteByUserIdAndToken(@Param("userId") Long userId, @Param("token") String token);

    void deleteByUserId(Long userId);

    long countByUserId(Long userId);

    List<FcmToken> findTop10ByUserIdAndLastSeenAtAfterOrderByLastSeenAtDesc(Long userId, LocalDateTime before);

    @Modifying
    @Query(value = """
            INSERT INTO fcm_token (user_id,token,device_info,created_at,updated_at,last_seen_at)
            VALUES (:userId,:token,:deviceInfo,:now,:now,:now)
            ON DUPLICATE KEY UPDATE user_id=:userId, device_info=:deviceInfo, updated_at=:now, last_seen_at=:now
            """, nativeQuery = true)
    void upsert(@Param("userId") Long userId, @Param("token") String token,
                @Param("deviceInfo") String deviceInfo, @Param("now") LocalDateTime now);
    @Override
    void delete(FcmToken entity);

    void deleteByUserIdAndLastSeenAtBefore(Long userId, LocalDateTime before);
}
