package com.example.RealMatch.attachment.domain.entity;

import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;
import com.example.RealMatch.global.common.DeleteBaseEntity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "attachment")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttachmentStatus status;

    // S3 prefix 분리용. CHAT/PUBLIC에 따라 경로가 나뉘며, 향후 용도별 TTL 분기 가능
    @Enumerated(EnumType.STRING)
    @Column(name = "usage", length = 20)
    private AttachmentUsage usage;

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
    }

    public static Attachment createReady(
            Long uploaderId,
            AttachmentType attachmentType,
            String contentType,
            String originalName,
            Long fileSize,
            @Nullable String storageKey,
            AttachmentUsage usage
    ) {
        return new Attachment(
                uploaderId,
                attachmentType,
                contentType,
                originalName,
                fileSize,
                storageKey,
                AttachmentStatus.READY,
                usage
        );
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

    public void updateStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void markAsReady() {
        this.status = AttachmentStatus.READY;
    }

    public void markAsFailed() {
        this.status = AttachmentStatus.FAILED;
    }
}
