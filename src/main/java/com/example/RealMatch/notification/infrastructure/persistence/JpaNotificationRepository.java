package com.example.RealMatch.notification.infrastructure.persistence;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.notification.application.repository.NotificationRepository;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;

public interface JpaNotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepository {

    // Include deleted inbox entries: replay must never recreate a notification the user deleted.
    @Query(value = "SELECT * FROM notification WHERE idempotency_key = :key FOR UPDATE", nativeQuery = true)
    Optional<Notification> findByIdempotencyKeyIncludingDeleted(@Param("key") String key);

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndKindIn(Long userId, Collection<NotificationKind> kinds, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false")
    long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 해당 유저의 미읽음 알림을 모두 읽음 처리한다 (벌크 UPDATE)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false")
    int markAllAsRead(@Param("userId") Long userId);
    @Override
    <S extends Notification> S save(S entity);
    @Override
    Optional<Notification> findById(UUID id);
}
