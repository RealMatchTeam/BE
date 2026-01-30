package com.example.RealMatch.attachment.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.infrastructure.storage.S3FileUploadService;
import com.example.RealMatch.attachment.infrastructure.storage.S3Properties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(S3FileUploadService.class)
public class AttachmentUrlService {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentUrlService.class);

    private final S3FileUploadService s3FileUploadService;
    private final S3Properties s3Properties;

    public String getAccessUrl(Attachment attachment) {
        if (attachment == null || attachment.getAccessUrl() == null) {
            return null;
        }

        String accessUrl = attachment.getAccessUrl();

        if (!s3Properties.isPublicBucket()) {
            try {
                accessUrl = s3FileUploadService.generatePresignedUrl(
                        accessUrl,
                        s3Properties.getPresignedUrlExpirationSeconds()
                );
            } catch (Exception e) {
                LOG.warn("Presigned URL 생성 실패. attachmentId={}, s3Key={}", 
                        attachment.getId(), accessUrl, e);
                accessUrl = null;
            }
        }

        return accessUrl;
    }
}
