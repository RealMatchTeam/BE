package com.example.RealMatch.notification.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.notification.application.repository.PushReceiptRepository;
import com.example.RealMatch.notification.domain.entity.PushReceipt;

public interface JpaPushReceiptRepository extends JpaRepository<PushReceipt, Long>, PushReceiptRepository {
    boolean existsByNotificationIdAndTokenId(UUID notificationId, Long tokenId);
    @Override
    <S extends PushReceipt> S saveAndFlush(S entity);
    @Query("select r.id from PushReceipt r where r.createdAt < :before and not exists (select d.id from NotificationDelivery d where d.notificationId = r.notificationId and d.status in ('PENDING','RETRY','IN_PROGRESS')) order by r.createdAt, r.id")
    List<Long> findCompleted(@Param("before") LocalDateTime before, Pageable page);
    void deleteAllByIdInBatch(Iterable<Long> ids);
}
