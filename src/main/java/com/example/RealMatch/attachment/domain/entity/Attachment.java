package com.example.RealMatch.attachment.domain.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;

import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;
import com.example.RealMatch.global.common.DeleteBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@SQLRestriction("is_deleted = false")
@Table(name = "attachment", indexes = {
        @Index(name = "idx_attachment_due", columnList = "is_deleted,next_cleanup_at,id"),
        @Index(name = "idx_attachment_status", columnList = "is_deleted,status"),
        @Index(name = "idx_attachment_uploader_created", columnList = "uploader_id,created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", nullable = false, length = 20)
    private AttachmentType attachmentType;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "storage_key", length = 1024)
    private String storageKey;

    @Column(name = "storage_key_hash", length = 64, unique = true)
    private String storageKeyHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private AttachmentStatus status;

    // S3 prefix 분리용. CHAT/PUBLIC에 따라 경로가 나뉘며, 향후 용도별 TTL 분기 가능
    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_usage", length = 20)
    private AttachmentUsage usage;

    private LocalDateTime retainedAt;
    private LocalDateTime claimedAt;
    private LocalDateTime nextCleanupAt;
    private int cleanupAttempts;
    @Column(columnDefinition = "BINARY(16)")
    private UUID cleanupToken;

    private Attachment(
            Long uploaderId,
            AttachmentType attachmentType,
            String contentType,
            String originalName,
            Long fileSize,
            String storageKey,
            AttachmentStatus status,
            AttachmentUsage usage
    ) {
        this.uploaderId = uploaderId;
        this.attachmentType = attachmentType;
        this.contentType = contentType;
        this.originalName = originalName;
        this.fileSize = fileSize;
        this.storageKey = storageKey;
        this.status = status;
        this.usage = usage;
        this.nextCleanupAt = LocalDateTime.now().plusMinutes(30);
    }

    public static Attachment createUploading(
            Long uploaderId,
            AttachmentType attachmentType,
            String contentType,
            String originalName,
            Long fileSize,
            AttachmentUsage usage
    ) {
        return new Attachment(
                uploaderId,
                attachmentType,
                contentType,
                originalName,
                fileSize,
                null,
                AttachmentStatus.UPLOADED,
                usage
        );
    }

    public void setStorageKey(String key) {
        if (status != AttachmentStatus.UPLOADED || storageKey != null || key == null || key.isBlank()) {
            throw new IllegalStateException("Storage key requires an uploading attachment");
        }
        storageKey = key;
        storageKeyHash = hashStorageKey(key);
    }

    public static String hashStorageKey(String key) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime", ex);
        }
    }

    public void ready() {
        if (status == AttachmentStatus.READY) {
            return;
        }
        if (status != AttachmentStatus.UPLOADED) {
            throw new IllegalStateException("Upload is no longer active");
        }
        status = AttachmentStatus.READY;
        nextCleanupAt = LocalDateTime.now().plusDays(1);
    }

    public void failUpload(LocalDateTime now) {
        if (status == AttachmentStatus.UPLOADED) {
            status = AttachmentStatus.FAILED;
            nextCleanupAt = now;
        }
    }

    public void retain(LocalDateTime now) {
        if (status != AttachmentStatus.READY) {
            throw new IllegalStateException("Only ready attachments can be retained");
        }
        retainedAt = now;
        nextCleanupAt = null;
    }

    public boolean cleanupDue(LocalDateTime now) {
        // A legacy row without an explicit deadline must never be mistaken for an orphan.
        return !isDeleted() && status != AttachmentStatus.DELETE_FAILED
                && !(status == AttachmentStatus.READY && retainedAt != null)
                && nextCleanupAt != null && !nextCleanupAt.isAfter(now);
    }

    public UUID claimCleanup(LocalDateTime now) {
        if (cleanupAttempts >= 10) {
            status = AttachmentStatus.DELETE_FAILED;
            cleanupToken = null;
            nextCleanupAt = null;
            return null;
        }
        status = AttachmentStatus.DELETE_PENDING;
        claimedAt = now;
        nextCleanupAt = now.plusMinutes(10);
        cleanupToken = UUID.randomUUID();
        cleanupAttempts++;
        return cleanupToken;
    }

    public void finishCleanup(UUID token, boolean success, LocalDateTime now) {
        if (status != AttachmentStatus.DELETE_PENDING || !Objects.equals(cleanupToken, token)) {
            return;
        }
        cleanupToken = null;
        if (success) {
            softDelete();
            nextCleanupAt = null;
        } else {
            status = cleanupAttempts >= 10 ? AttachmentStatus.DELETE_FAILED : AttachmentStatus.FAILED;
            nextCleanupAt = status == AttachmentStatus.DELETE_FAILED ? null
                    : now.plusMinutes(Math.min(1L << Math.min(cleanupAttempts, 10), 720));
        }
    }
}
