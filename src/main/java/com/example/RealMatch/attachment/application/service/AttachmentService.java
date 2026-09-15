package com.example.RealMatch.attachment.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.dto.request.AttachmentUploadRequest;
import com.example.RealMatch.attachment.application.dto.response.AttachmentUploadResponse;
import com.example.RealMatch.attachment.application.mapper.AttachmentResponseMapper;
import com.example.RealMatch.attachment.application.port.AttachmentStorage;
import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentService.class);

    private final Semaphore uploads = new Semaphore(4);

    private final AttachmentUploadTxService uploadTxService;
    private final AttachmentStorage s3FileUploadService;
    private final AttachmentCommandService attachmentCommandService;
    private final AttachmentValidationService attachmentValidationService;
    private final AttachmentUrlService attachmentUrlService;
    private final AttachmentResponseMapper responseMapper;

    public AttachmentUploadResponse uploadAttachment(
            Long userId,
            AttachmentUploadRequest request,
            InputStream fileInputStream,
            String originalFilename,
            String contentType,
            long fileSize
    ) {
        String normalizedContentType = attachmentValidationService.validateUploadRequest(
                originalFilename,
                contentType,
                fileSize,
                request.attachmentType()
        );

        if (!s3FileUploadService.isAvailable()) {
            throw new CustomException(AttachmentErrorCode.STORAGE_UNAVAILABLE);
        }
        if (request.usage() == null) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE);
        }
        if (!uploads.tryAcquire()) {
            throw new CustomException(AttachmentErrorCode.UPLOAD_LIMIT_EXCEEDED);
        }
        Path staged = null;
        Long attachmentId = null;
        try {
            staged = attachmentValidationService.stage(fileInputStream, originalFilename, normalizedContentType, fileSize);
            var created = uploadTxService.createAttachmentAndSetStorageKey(userId, request.attachmentType(),
                    normalizedContentType, originalFilename, fileSize, request.usage());
            attachmentId = created.attachment().getId();
            try (var input = Files.newInputStream(staged)) {
                s3FileUploadService.uploadFile(input, created.s3Key(), normalizedContentType, fileSize, request.usage());
            }
            var ready = uploadTxService.markAttachmentAsReady(attachmentId);
            return responseMapper.toUploadResponse(ready, attachmentUrlService.getAccessUrl(ready));
        } catch (Exception ex) {
            if (attachmentId != null) {
                safeMarkFailed(attachmentId);
            }
            if (ex instanceof CustomException custom) {
                throw custom;
            }
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED, ex);
        } finally {
            uploads.release();
            if (staged != null) {
                try {
                    Files.deleteIfExists(staged);
                } catch (IOException ex) {
                    LOG.error("Cannot remove staged attachment {}", staged, ex);
                }
            }
        }
    }

    private void safeMarkFailed(Long attachmentId) {
        try {
            attachmentCommandService.markAttachmentAsFailed(attachmentId);
        } catch (Exception ex) {
            LOG.error("FAILED 처리 실패. attachmentId={}", attachmentId, ex);
        }
    }
}
