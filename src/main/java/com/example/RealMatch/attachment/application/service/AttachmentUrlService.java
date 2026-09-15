package com.example.RealMatch.attachment.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.port.AttachmentStorage;
import com.example.RealMatch.attachment.application.repository.AttachmentRepository;
import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentUrlService {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentUrlService.class);

    private final AttachmentStorage s3FileUploadService;
    private final AttachmentRepository attachments;

    /**
     * Attachment 엔티티 기반 URL 반환
     * PUBLIC → CloudFront URL, CHAT → presigned URL.
     */
    public String getAccessUrl(Attachment attachment) {
        if (attachment == null || attachment.getStatus() != AttachmentStatus.READY) {
            return null;
        }
        String storageKey = attachment.getStorageKey();
        if (storageKey == null || storageKey.isBlank()) {
            LOG.error("READY attachment has no storage key. attachmentId={}", attachment.getId());
            throw new CustomException(AttachmentErrorCode.ATTACHMENT_INVALID_STATUS);
        }
        return getAccessUrl(storageKey, attachment.getUsage());
    }

    /**
     * storageKey + usage로 URL 반환
     * PUBLIC → CloudFront URL, CHAT → presigned URL
     */
    public String getAccessUrl(String storageKey, AttachmentUsage usage) {
        if (storageKey == null || storageKey.isBlank()) {
            return null;
        }
        if (usage == null) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE);
        }
        if (usage == AttachmentUsage.PUBLIC) {
            String url = s3FileUploadService.buildPublicUrl(storageKey);
            if (url == null || url.isBlank()) {
                throw new CustomException(AttachmentErrorCode.STORAGE_UNAVAILABLE);
            }
            return url;
        }
        return generatePresignedUrl(storageKey);
    }

    /**
     * storageKey만으로 URL 반환
     * DB에 저장된 usage로 접근 정책을 결정한다.
     */
    public String getAccessUrl(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return null;
        }
        // 이미 CloudFront URL이 저장된 경우, 신뢰 도메인(cloudfrontBaseUrl)인 경우만 그대로 반환
        if (storageKey.startsWith("http://") || storageKey.startsWith("https://")) {
            String key = s3FileUploadService.publicStorageKey(storageKey);
            if (key == null) {
                throw new CustomException(AttachmentErrorCode.INVALID_FILE);
            }
            return getAccessUrl(key, AttachmentUsage.PUBLIC);
        }
        return attachments.findByStorageKey(storageKey).map(this::getAccessUrl)
                .orElseThrow(() -> new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
    }

    private String generatePresignedUrl(String storageKey) {
        if (!s3FileUploadService.isAvailable()) {
            throw new CustomException(AttachmentErrorCode.STORAGE_UNAVAILABLE);
        }
        try {
            return s3FileUploadService.generatePresignedUrl(storageKey);
        } catch (Exception e) {
            LOG.warn("Presigned URL 생성 실패. s3Key={}", storageKey, e);
            throw new CustomException(AttachmentErrorCode.STORAGE_UNAVAILABLE, e);
        }
    }
}
