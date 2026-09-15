package com.example.RealMatch.attachment.application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;

public interface AttachmentRepository {

    Optional<Attachment> findForUpdate(Long id);

    Optional<Attachment> findByStorageKeyHash(String hash);

    default Optional<Attachment> findByStorageKey(String storageKey) {
        return findByStorageKeyHash(Attachment.hashStorageKey(storageKey))
                .filter(attachment -> storageKey.equals(attachment.getStorageKey()));
    }

    List<Long> findCleanupCandidates(LocalDateTime now, Pageable page);

    long countByUploaderIdAndCreatedAtAfter(Long uploaderId, LocalDateTime since);

    long countByStatus(AttachmentStatus status);
    <S extends Attachment> S save(S entity);
    Optional<Attachment> findById(Long id);
    List<Attachment> findAllById(Iterable<Long> ids);
}
