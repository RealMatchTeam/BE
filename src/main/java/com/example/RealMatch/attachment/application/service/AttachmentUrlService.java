package com.example.RealMatch.attachment.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.infrastructure.storage.S3FileUploadService;
import com.example.RealMatch.attachment.infrastructure.storage.S3Properties;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(S3FileUploadService.class)
public class AttachmentUrlService {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentUrlService.class);

    private final S3FileUploadService s3FileUploadService;
    private final S3Properties s3Properties;

    public String getAccessUrl(Attachment attachment) {
        if (attachment == null || attachment.getStatus() != AttachmentStatus.READY) {
            return null;
        }
        if (!s3FileUploadService.isAvailable()) {
            throw new CustomException(AttachmentErrorCode.STORAGE_UNAVAILABLE);
        }

        String storageKey = attachment.getStorageKey();
        if (storageKey == null || storageKey.isBlank()) {
            LOG.error("READY 상태인데 storageKey가 없습니다. attachmentId={}", attachment.getId());
            return null;
        }
        try {
            return s3FileUploadService.generatePresignedUrl(
                    storageKey,
                    s3Properties.getPresignedUrlExpirationSeconds()
            );
        } catch (Exception e) {
            LOG.warn("Presigned URL 생성 실패. attachmentId={}, s3Key={}",
                    attachment.getId(), storageKey, e);
            return null;
        }
    }
}
