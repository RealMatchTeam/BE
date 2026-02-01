package com.example.RealMatch.attachment.infrastructure.storage;

import java.io.InputStream;

import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;

public interface S3FileUploadService {

    String uploadFile(InputStream inputStream, String key, String contentType, long fileSize);

    String generatePresignedUrl(String key, int expirationSeconds);

    String generateS3Key(AttachmentUsage usage, Long userId, Long attachmentId, String originalFilename);

    void deleteFile(String key);

    default boolean isAvailable() {
        return true;
    }
}
