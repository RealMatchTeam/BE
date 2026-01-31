package com.example.RealMatch.attachment.domain.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Attachment a
               set a.deletedAt = :deletedAt,
                   a.isDeleted = true
             where a.status = :status
               and a.createdAt < :before
               and a.isDeleted = false
            """)
    int softDeleteByStatusAndCreatedAtBefore(
            @Param("status") AttachmentStatus status,
            @Param("before") LocalDateTime before,
            @Param("deletedAt") LocalDateTime deletedAt
    );
}
