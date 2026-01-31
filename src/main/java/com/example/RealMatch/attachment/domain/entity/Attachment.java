package com.example.RealMatch.attachment.domain.entity;

import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.global.common.DeleteBaseEntity;

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

    @Column(name = "access_url", length = 1024)
    private String accessUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttachmentStatus status;

    private Attachment(
            Long uploaderId,
            AttachmentType attachmentType,
            String contentType,
            String originalName,
            Long fileSize,
            String storageKey,
            String accessUrl,
            AttachmentStatus status
    ) {
        this.uploaderId = uploaderId;
        this.attachmentType = attachmentType;
        this.contentType = contentType;
        this.originalName = originalName;
        this.fileSize = fileSize;
        this.storageKey = storageKey;
        this.accessUrl = accessUrl;
        this.status = status;
    }

    public static Attachment create(
            Long uploaderId,
            AttachmentType attachmentType,
            String contentType,
            String originalName,
            Long fileSize,
            String accessUrl
    ) {
        return new Attachment(
                uploaderId,
                attachmentType,
                contentType,
                originalName,
                fileSize,
                null,
                accessUrl,
                AttachmentStatus.UPLOADED
        );
    }

    public void updateStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void markAsReady(String accessUrl) {
        this.accessUrl = accessUrl;
        this.status = AttachmentStatus.READY;
    }

    public void markAsFailed() {
        this.status = AttachmentStatus.FAILED;
    }

    public void updateAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }
}
