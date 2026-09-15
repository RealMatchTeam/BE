package com.example.RealMatch.notification.application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.example.RealMatch.notification.domain.entity.PushReceipt;

public interface PushReceiptRepository {
    boolean existsByNotificationIdAndTokenId(UUID notificationId, Long tokenId);
    <S extends PushReceipt> S saveAndFlush(S entity);
    List<Long> findCompleted(LocalDateTime before, Pageable page);
    void deleteAllByIdInBatch(Iterable<Long> ids);
}
