package com.example.RealMatch.attachment.infrastructure.storage;

import java.io.InputStream;

public interface S3FileUploadService {

    String uploadFile(InputStream inputStream, String key, String contentType, long fileSize);

    String generatePresignedUrl(String key, int expirationSeconds);

    String generateS3Key(Long userId, Long attachmentId, String originalFilename);
}
