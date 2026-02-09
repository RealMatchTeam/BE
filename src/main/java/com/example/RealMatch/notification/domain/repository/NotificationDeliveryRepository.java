package com.example.RealMatch.notification.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {

    Optional<NotificationDelivery> findByNotificationIdAndChannel(UUID notificationId, NotificationChannel channel);

    @Query("SELECT nd FROM NotificationDelivery nd "
            + "WHERE nd.status = :status "
            + "AND (nd.nextRetryAt IS NULL OR nd.nextRetryAt <= :now) "
            + "ORDER BY nd.createdAt ASC")
    List<NotificationDelivery> findRetryableDeliveries(
            @Param("status") DeliveryStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationDelivery nd SET nd.status = :newStatus, nd.nextRetryAt = null "
            + "WHERE nd.status = :stuckStatus AND nd.attemptedAt <= :stuckBefore")
    int recoverStuckDeliveries(
            @Param("stuckStatus") DeliveryStatus stuckStatus,
            @Param("newStatus") DeliveryStatus newStatus,
            @Param("stuckBefore") LocalDateTime stuckBefore);
}
