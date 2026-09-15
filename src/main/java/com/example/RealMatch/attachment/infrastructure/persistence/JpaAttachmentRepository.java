package com.example.RealMatch.attachment.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.attachment.application.repository.AttachmentRepository;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;

import jakarta.persistence.LockModeType;

public interface JpaAttachmentRepository extends JpaRepository<Attachment, Long>, AttachmentRepository {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Attachment a where a.id = :id")
    Optional<Attachment> findForUpdate(@Param("id") Long id);

    Optional<Attachment> findByStorageKeyHash(String hash);

    @Query("""
            select a.id from Attachment a where a.nextCleanupAt <= :now
            order by a.nextCleanupAt, a.id
            """)
    List<Long> findCleanupCandidates(@Param("now") LocalDateTime now, Pageable page);

    // Failed/deleted uploads still consume the quota; @Where must not exclude them.
    @Query(value = "select count(*) from attachment where uploader_id = :uploaderId and created_at > :since", nativeQuery = true)
    long countByUploaderIdAndCreatedAtAfter(@Param("uploaderId") Long uploaderId, @Param("since") LocalDateTime since);

    long countByStatus(AttachmentStatus status);
    @Override
    <S extends Attachment> S save(S entity);
    @Override
    Optional<Attachment> findById(Long id);
    @Override
    List<Attachment> findAllById(Iterable<Long> ids);
}
