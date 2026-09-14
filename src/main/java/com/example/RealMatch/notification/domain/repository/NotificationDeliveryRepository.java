package com.example.RealMatch.notification.domain.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationDelivery nd "
            + "SET nd.status = :targetStatus, nd.attemptedAt = :now "
            + "WHERE nd.id = :id AND nd.status IN :sourceStatuses "
            + "AND (nd.nextRetryAt IS NULL OR nd.nextRetryAt <= :now)")
    int claimDelivery(
            @Param("id") UUID id,
            @Param("targetStatus") DeliveryStatus targetStatus,
            @Param("now") LocalDateTime now,
            @Param("sourceStatuses") Collection<DeliveryStatus> sourceStatuses);

    @Query("SELECT nd FROM NotificationDelivery nd "
            + "WHERE nd.status = :status "
            + "AND (nd.nextRetryAt IS NULL OR nd.nextRetryAt <= :now) "
            + "ORDER BY nd.createdAt ASC")
    List<NotificationDelivery> findRetryableDeliveries(
            @Param("status") DeliveryStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationDelivery nd "
            + "SET nd.status = :newStatus, nd.nextRetryAt = null "
            + "WHERE nd.status = :stuckStatus AND nd.attemptedAt <= :stuckBefore")
    int recoverStuckDeliveries(
            @Param("stuckStatus") DeliveryStatus stuckStatus,
            @Param("newStatus") DeliveryStatus newStatus,
            @Param("stuckBefore") LocalDateTime stuckBefore);

    @Query("SELECT nd FROM NotificationDelivery nd "
            + "WHERE nd.status = :status "
            + "AND nd.createdAt <= :orphanBefore "
            + "AND NOT EXISTS ("
            + "  SELECT 1 FROM NotificationOutbox o "
            + "  WHERE o.deliveryId = nd.id "
            + "  AND o.status IN ('PENDING', 'SENDING')"
            + ") "
            + "ORDER BY nd.createdAt ASC")
    List<NotificationDelivery> findOrphanedPendingDeliveries(
            @Param("status") DeliveryStatus status,
            @Param("orphanBefore") LocalDateTime orphanBefore,
            Pageable pageable);
}
