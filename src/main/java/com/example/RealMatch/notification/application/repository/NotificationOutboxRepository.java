package com.example.RealMatch.notification.application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.domain.entity.enums.OutboxStatus;

public interface NotificationOutboxRepository {

    Optional<NotificationOutbox> findForUpdate( UUID id);

    Optional<NotificationOutbox> findByDeliveryId(UUID deliveryId);

    List<NotificationOutbox> findDue(LocalDateTime now, Pageable page);

    List<UUID> findStuck(LocalDateTime before, Pageable page);

    List<UUID> findCompleted(LocalDateTime before, Pageable page);

    long countByStatus(OutboxStatus status);

    <S extends NotificationOutbox> S save(S entity);
    void deleteAllByIdInBatch(Iterable<UUID> ids);
    LocalDateTime oldestPending();
}
