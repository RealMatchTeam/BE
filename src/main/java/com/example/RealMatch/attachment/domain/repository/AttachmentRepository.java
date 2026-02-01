package com.example.RealMatch.attachment.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    @Query("""
            select a.id
              from Attachment a
             where a.status = :status
               and a.createdAt < :before
               and a.isDeleted = false
             order by a.id
            """)
    List<Long> findIdsByStatusAndCreatedAtBefore(
            @Param("status") AttachmentStatus status,
            @Param("before") LocalDateTime before,
            Pageable pageable
    );

    @Query("""
            select a.id as id, a.storageKey as storageKey
              from Attachment a
             where a.id in :ids
               and a.status = :status
               and a.isDeleted = false
            """)
    List<AttachmentCleanupTarget> findCleanupTargetsByIdInAndStatus(
            @Param("ids") List<Long> ids,
            @Param("status") AttachmentStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Attachment a
               set a.status = :toStatus
             where a.id = :id
               and a.status = :fromStatus
               and a.isDeleted = false
            """)
    int updateStatusByIdAndStatus(
            @Param("id") Long id,
            @Param("fromStatus") AttachmentStatus fromStatus,
            @Param("toStatus") AttachmentStatus toStatus
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Attachment a
               set a.status = :toStatus
             where a.id in :ids
               and a.status = :fromStatus
               and a.isDeleted = false
            """)
    int updateStatusByIdsAndStatus(
            @Param("ids") List<Long> ids,
            @Param("fromStatus") AttachmentStatus fromStatus,
            @Param("toStatus") AttachmentStatus toStatus
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Attachment a
               set a.storageKey = :storageKey
             where a.id = :id
               and a.status = :status
               and a.isDeleted = false
            """)
    int updateStorageKeyIfStatus(
            @Param("id") Long id,
            @Param("status") AttachmentStatus status,
            @Param("storageKey") String storageKey
    );

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Attachment a
               set a.deletedAt = :deletedAt,
                   a.isDeleted = true,
                   a.status = :toStatus
             where a.id in :ids
               and a.status = :fromStatus
               and a.isDeleted = false
            """)
    int softDeleteByIdsAndStatus(
            @Param("ids") List<Long> ids,
            @Param("fromStatus") AttachmentStatus fromStatus,
            @Param("toStatus") AttachmentStatus toStatus,
            @Param("deletedAt") LocalDateTime deletedAt
    );

    interface AttachmentCleanupTarget {
        Long getId();
        String getStorageKey();
    }
}
