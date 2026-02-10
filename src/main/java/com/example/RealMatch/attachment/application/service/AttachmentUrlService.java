package com.example.RealMatch.attachment.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;
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
            LOG.error("READY 상태인데 storageKey가 없습니다. attachmentId={}", attachment.getId());
            return null;
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
        if (usage == AttachmentUsage.PUBLIC) {
            return s3FileUploadService.buildPublicUrl(storageKey);
        }
        return generatePresignedUrl(storageKey);
    }

    /**
     * storageKey만으로 URL 반환
     * storageKey 경로에 /public/이 포함되면 PUBLIC으로 간주.
     */
    public String getAccessUrl(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return null;
        }
        // 이미 CloudFront URL이 저장된 경우, 신뢰 도메인(cloudfrontBaseUrl)인 경우만 그대로 반환
        if (storageKey.startsWith("http://") || storageKey.startsWith("https://")) {
            return isTrustedRedirectUrl(storageKey) ? storageKey : null;
        }
        AttachmentUsage inferred = storageKey.contains("/public/")
                ? AttachmentUsage.PUBLIC
                : AttachmentUsage.CHAT;
        return getAccessUrl(storageKey, inferred);
    }

    /**
     * DB에 저장된 URL이 우리 CloudFront 등 신뢰 도메인인지 검사. open redirect 방지.
     */
    private boolean isTrustedRedirectUrl(String url) {
        String base = s3Properties.getCloudfrontBaseUrl();
        if (base == null || base.isBlank()) {
            return false;
        }
        String baseNormalized = base.trim().toLowerCase();
        if (baseNormalized.endsWith("/")) {
            baseNormalized = baseNormalized.substring(0, baseNormalized.length() - 1);
        }
        String urlLower = url.trim().toLowerCase();
        return urlLower.equals(baseNormalized) || urlLower.startsWith(baseNormalized + "/");
    }

    private String generatePresignedUrl(String storageKey) {
        if (!s3FileUploadService.isAvailable()) {
            throw new CustomException(AttachmentErrorCode.STORAGE_UNAVAILABLE);
        }
        try {
            return s3FileUploadService.generatePresignedUrl(
                    storageKey,
                    s3Properties.getPresignedUrlExpirationSeconds()
            );
        } catch (Exception e) {
            LOG.warn("Presigned URL 생성 실패. s3Key={}", storageKey, e);
            return null;
        }
    }
}
