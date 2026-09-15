package com.example.RealMatch.notification.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.notification.application.repository.NotificationOutboxRepository;
import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.domain.entity.enums.OutboxStatus;

import jakarta.persistence.LockModeType;

public interface JpaNotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID>, NotificationOutboxRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from NotificationOutbox o where o.id = :id")
    Optional<NotificationOutbox> findForUpdate(@Param("id") UUID id);

    Optional<NotificationOutbox> findByDeliveryId(UUID deliveryId);

    @Query("select o from NotificationOutbox o where o.status = 'PENDING' and o.nextAttemptAt <= :now order by o.nextAttemptAt, o.createdAt, o.id")
    List<NotificationOutbox> findDue(@Param("now") LocalDateTime now, Pageable page);

    @Query("select o.id from NotificationOutbox o where o.status = 'SENDING' and o.attemptedAt < :before order by o.attemptedAt")
    List<UUID> findStuck(@Param("before") LocalDateTime before, Pageable page);

    @Query("select o.id from NotificationOutbox o where o.status in ('SENT','FAILED') and o.updatedAt < :before and exists (select d.id from NotificationDelivery d where d.id = o.deliveryId and d.status in ('SENT','SKIPPED','FAILED')) order by o.updatedAt")
    List<UUID> findCompleted(@Param("before") LocalDateTime before, Pageable page);

    long countByStatus(OutboxStatus status);

    @Override
    <S extends NotificationOutbox> S save(S entity);
    @Override
    void deleteAllByIdInBatch(Iterable<UUID> ids);
    @Query("select min(d.createdAt) from NotificationOutbox d where d.status in ('PENDING','SENDING')")
    LocalDateTime oldestPending();
}
