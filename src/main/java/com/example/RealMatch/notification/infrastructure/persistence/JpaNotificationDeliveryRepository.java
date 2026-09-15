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

import com.example.RealMatch.notification.application.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;

import jakarta.persistence.LockModeType;

public interface JpaNotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID>, NotificationDeliveryRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from NotificationDelivery d where d.id = :id")
    Optional<NotificationDelivery> findForUpdate(@Param("id") UUID id);

    @Query("""
            select d from NotificationDelivery d where d.status in ('PENDING','RETRY')
            and (d.nextRetryAt is null or d.nextRetryAt <= :now)
            and (d.lastEnqueuedAt is null or d.lastEnqueuedAt < :before
                 or (d.nextRetryAt is not null and d.lastEnqueuedAt < d.attemptedAt))
            and not exists (select o.id from NotificationOutbox o where o.deliveryId = d.id
                            and o.status in ('PENDING','SENDING'))
            order by d.createdAt, d.id
            """)
    List<NotificationDelivery> findDispatchable(@Param("now") LocalDateTime now,
                                               @Param("before") LocalDateTime before, Pageable page);

    @Query("select d.id from NotificationDelivery d where d.status = 'IN_PROGRESS' and d.attemptedAt < :before order by d.attemptedAt")
    List<UUID> findStuck(@Param("before") LocalDateTime before, Pageable page);

    long countByStatus(DeliveryStatus status);

    @Override
    <S extends NotificationDelivery> S save(S entity);
    @Query("select min(d.createdAt) from NotificationDelivery d where d.status in ('PENDING','RETRY')")
    LocalDateTime oldestPending();
}
