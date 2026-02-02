package com.example.RealMatch.attachment.infrastructure.storage;

import java.io.InputStream;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;
import com.example.RealMatch.global.exception.CustomException;

@Service
@Profile("!prod")
@Conditional(S3CredentialsMissingCondition.class)
public class NoOpS3FileUploadService implements S3FileUploadService {

    @Override
    public String uploadFile(InputStream inputStream, String key, String contentType, long fileSize) {
        throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED, "S3 is not configured.");
    }

    @Override
    public String generatePresignedUrl(String key, int expirationSeconds) {
        throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED, "S3 is not configured.");
    }

    @Override
    public String generateS3Key(AttachmentUsage usage, Long userId, Long attachmentId, String originalFilename) {
        throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED, "S3 is not configured.");
    }

    @Override
    public void deleteFile(String key) {
        throw new CustomException(AttachmentErrorCode.S3_DELETE_FAILED, "S3 is not configured.");
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
