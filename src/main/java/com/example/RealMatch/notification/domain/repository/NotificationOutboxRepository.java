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

import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.domain.entity.enums.OutboxStatus;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {

    /** PENDING Outbox 조회 (생성일 ASC). */
    @Query("SELECT o FROM NotificationOutbox o "
            + "WHERE o.status = :status "
            + "ORDER BY o.createdAt ASC")
    List<NotificationOutbox> findByStatusOrderByCreatedAtAsc(
            @Param("status") OutboxStatus status,
            Pageable pageable);

    /** PENDING → SENDING claim. 1이면 성공. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationOutbox o "
            + "SET o.status = :targetStatus "
            + "WHERE o.id = :id AND o.status = :sourceStatus")
    int claimOutbox(
            @Param("id") UUID id,
            @Param("targetStatus") OutboxStatus targetStatus,
            @Param("sourceStatus") OutboxStatus sourceStatus);

    /** SENDING → SENT. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationOutbox o "
            + "SET o.status = :targetStatus "
            + "WHERE o.id = :id AND o.status = :sourceStatus")
    int markAsSent(
            @Param("id") UUID id,
            @Param("targetStatus") OutboxStatus targetStatus,
            @Param("sourceStatus") OutboxStatus sourceStatus);

    /** 발행 실패: retryCount 증가 + status를 DB에서 원자적으로 결정 (레이스 방지). enum은 파라미터로 전달해 provider-agnostic. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationOutbox o "
            + "SET o.retryCount = o.retryCount + 1, "
            + "o.lastError = :lastError, "
            + "o.status = CASE WHEN (o.retryCount + 1) >= :maxRetry THEN :statusFailed ELSE :statusPending END "
            + "WHERE o.id = :id AND o.status IN :sourceStatuses")
    int markPublishFailed(
            @Param("id") UUID id,
            @Param("lastError") String lastError,
            @Param("maxRetry") int maxRetry,
            @Param("statusFailed") OutboxStatus statusFailed,
            @Param("statusPending") OutboxStatus statusPending,
            @Param("sourceStatuses") Collection<OutboxStatus> sourceStatuses);

    /** 미처리 Outbox(PENDING/SENDING) 존재 여부. 중복 생성 방지용. */
    boolean existsByDeliveryIdAndStatusIn(UUID deliveryId, Collection<OutboxStatus> statuses);

    /** stuck SENDING → PENDING 복구. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationOutbox o "
            + "SET o.status = :newStatus "
            + "WHERE o.status = :stuckStatus AND o.updatedAt <= :stuckBefore")
    int recoverStuckOutbox(
            @Param("stuckStatus") OutboxStatus stuckStatus,
            @Param("newStatus") OutboxStatus newStatus,
            @Param("stuckBefore") LocalDateTime stuckBefore);

    /** 완료(SENT/FAILED)된 오래된 Outbox 삭제. */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM NotificationOutbox o "
            + "WHERE o.status IN :statuses AND o.updatedAt <= :before")
    int deleteCompletedOutboxBefore(
            @Param("statuses") Collection<OutboxStatus> statuses,
            @Param("before") LocalDateTime before);
}
