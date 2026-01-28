package com.example.RealMatch.attachment.application.service;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.util.FileValidator;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.exception.AttachmentException;
import com.example.RealMatch.attachment.domain.repository.AttachmentRepository;
import com.example.RealMatch.attachment.infrastructure.storage.S3FileUploadService;
import com.example.RealMatch.attachment.infrastructure.storage.S3Properties;
import com.example.RealMatch.attachment.presentation.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.presentation.dto.request.AttachmentUploadRequest;
import com.example.RealMatch.attachment.presentation.dto.response.AttachmentUploadResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    private final AttachmentRepository attachmentRepository;
    private final S3FileUploadService s3FileUploadService;
    private final S3Properties s3Properties;

    @Override
    @Transactional
    public AttachmentUploadResponse uploadAttachment(
            Long userId,
            AttachmentUploadRequest request,
            InputStream fileInputStream,
            String originalFilename,
            String contentType,
            long fileSize
    ) {
        AttachmentType attachmentType = request.attachmentType();

        long maxSize = attachmentType == AttachmentType.IMAGE
                ? s3Properties.getMaxImageSizeBytes()
                : s3Properties.getMaxFileSizeBytes();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_FILE_NAME);
        }
        FileValidator.validateFileName(originalFilename);
        FileValidator.validateFileSize(fileSize, maxSize);

        if (attachmentType == AttachmentType.IMAGE) {
            FileValidator.validateImageFile(contentType, originalFilename);
        }

        Attachment attachment = Attachment.create(
                userId,
                attachmentType,
                contentType,
                originalFilename,
                fileSize,
                null
        );
        attachment = attachmentRepository.save(attachment);

        String s3Key = null;
        String accessUrl = null;

        try {
            s3Key = s3FileUploadService.generateS3Key(userId, attachment.getId(), originalFilename);

            accessUrl = s3FileUploadService.uploadFile(
                    fileInputStream,
                    s3Key,
                    contentType,
                    fileSize
            );

            if (accessUrl != null) {
                attachment.updateAccessUrl(accessUrl);
            } else {
                attachment.updateAccessUrl(s3Key);
            }

            LOG.info("파일 업로드 성공. attachmentId={}, userId={}, s3Key={}", 
                    attachment.getId(), userId, s3Key);

        } catch (Exception e) {
            attachment.markAsFailed();
            LOG.error("파일 업로드 실패. attachmentId={}, userId={}, s3Key={}", 
                    attachment.getId(), userId, s3Key, e);
            throw e;
        }

        String responseAccessUrl = attachment.getAccessUrl();
        if (!s3Properties.isPublicBucket() && responseAccessUrl != null) {
            try {
                responseAccessUrl = s3FileUploadService.generatePresignedUrl(
                        responseAccessUrl,
                        s3Properties.getPresignedUrlExpirationSeconds()
                );
            } catch (Exception e) {
                LOG.warn("Presigned URL 생성 실패. attachmentId={}, s3Key={}", 
                        attachment.getId(), responseAccessUrl, e);
                responseAccessUrl = null;
            }
        }

        return new AttachmentUploadResponse(
                attachment.getId(),
                attachment.getAttachmentType(),
                attachment.getContentType(),
                attachment.getOriginalName(),
                attachment.getFileSize(),
                responseAccessUrl,
                attachment.getStatus(),
                attachment.getCreatedAt()
        );
    }
}
