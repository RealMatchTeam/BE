package com.example.RealMatch.notification.application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;

public interface NotificationDeliveryRepository {

    Optional<NotificationDelivery> findForUpdate( UUID id);

    List<NotificationDelivery> findDispatchable(LocalDateTime now,
                                                LocalDateTime before, Pageable page);

    List<UUID> findStuck(LocalDateTime before, Pageable page);

    long countByStatus(DeliveryStatus status);

    <S extends NotificationDelivery> S save(S entity);
    LocalDateTime oldestPending();
}
