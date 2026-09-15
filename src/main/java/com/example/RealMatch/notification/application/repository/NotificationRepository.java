package com.example.RealMatch.notification.application.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;

public interface NotificationRepository {

    // Include deleted inbox entries: replay must never recreate a notification the user deleted.

    Optional<Notification> findByIdempotencyKeyIncludingDeleted(String key);

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndKindIn(Long userId, Collection<NotificationKind> kinds, Pageable pageable);

    long countUnreadByUserId(Long userId);

    /**
     * 해당 유저의 미읽음 알림을 모두 읽음 처리한다 (벌크 UPDATE)
     */

    int markAllAsRead(Long userId);
    <S extends Notification> S save(S entity);
    Optional<Notification> findById(UUID id);
}
