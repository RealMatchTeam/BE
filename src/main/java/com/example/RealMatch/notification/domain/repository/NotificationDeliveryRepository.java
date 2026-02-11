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

    /** 조건절 UPDATE로 claim. 1이면 성공, 0이면 이미 처리 중/완료. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationDelivery nd "
            + "SET nd.status = :targetStatus, nd.attemptedAt = :now "
            + "WHERE nd.id = :id AND nd.status IN :sourceStatuses")
    int claimDelivery(
            @Param("id") UUID id,
            @Param("targetStatus") DeliveryStatus targetStatus,
            @Param("now") LocalDateTime now,
            @Param("sourceStatuses") Collection<DeliveryStatus> sourceStatuses);

    /** RETRY + backoff 만료된 delivery 조회. RecoveryScheduler용. */
    @Query("SELECT nd FROM NotificationDelivery nd "
            + "WHERE nd.status = :status "
            + "AND (nd.nextRetryAt IS NULL OR nd.nextRetryAt <= :now) "
            + "ORDER BY nd.createdAt ASC")
    List<NotificationDelivery> findRetryableDeliveries(
            @Param("status") DeliveryStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    /** stuck IN_PROGRESS → RETRY 복구. Consumer crash 대비. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationDelivery nd "
            + "SET nd.status = :newStatus, nd.nextRetryAt = null "
            + "WHERE nd.status = :stuckStatus AND nd.attemptedAt <= :stuckBefore")
    int recoverStuckDeliveries(
            @Param("stuckStatus") DeliveryStatus stuckStatus,
            @Param("newStatus") DeliveryStatus newStatus,
            @Param("stuckBefore") LocalDateTime stuckBefore);

    /** 활성 Outbox 없는 고아 PENDING delivery 조회. Outbox FAILED 후 복구용. */
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
