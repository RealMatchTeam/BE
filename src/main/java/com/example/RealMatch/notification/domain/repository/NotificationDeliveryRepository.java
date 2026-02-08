package com.example.RealMatch.notification.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {

    Optional<NotificationDelivery> findByNotificationIdAndChannel(UUID notificationId, NotificationChannel channel);

    @Query("SELECT nd FROM NotificationDelivery nd WHERE nd.status = :status ORDER BY nd.attemptedAt ASC")
    List<NotificationDelivery> findByStatusOrderByAttemptedAtAsc(@Param("status") DeliveryStatus status);
}
