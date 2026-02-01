package com.example.RealMatch.attachment.application.service;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.mapper.AttachmentResponseMapper;
import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.infrastructure.storage.S3CredentialsCondition;
import com.example.RealMatch.attachment.infrastructure.storage.S3FileUploadService;
import com.example.RealMatch.attachment.presentation.dto.request.AttachmentUploadRequest;
import com.example.RealMatch.attachment.presentation.dto.response.AttachmentUploadResponse;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Conditional(S3CredentialsCondition.class)
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    private final AttachmentUploadTxService uploadTxService;
    private final S3FileUploadService s3FileUploadService;
    private final AttachmentCommandService attachmentCommandService;
    private final AttachmentValidationService attachmentValidationService;
    private final AttachmentUrlService attachmentUrlService;
    private final AttachmentResponseMapper responseMapper;

    @Override
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

        // TX#1: 메타 생성(UPLOADED) + storageKey 저장
        AttachmentUploadTxService.CreateResult created = uploadTxService.createAttachmentAndSetStorageKey(
                userId,
                request.attachmentType(),
                normalizedContentType,
                originalFilename,
                fileSize
        );
        Attachment attachment = created.attachment();
        String s3Key = created.s3Key();

        try {
            // TX 밖: S3 업로드 (DB 커넥션 점유 없음)
            String accessUrl = s3FileUploadService.uploadFile(
                    fileInputStream,
                    s3Key,
                    normalizedContentType,
                    fileSize
            );

            // TX#2: UPLOADED → READY (조건부). markReady만 별도 catch → "S3 성공 + READY 전환 실패"일 때만 FAILED + S3 삭제 시도.
            try {
                attachment = uploadTxService.markAttachmentAsReady(attachment.getId(), accessUrl);
            } catch (CustomException readyEx) {
                // S3 성공 + READY 전환 실패 (DB 경합/상태 이상) → 여기서만 FAILED + S3 삭제, 그 다음 rethrow로 종료
                safeMarkFailed(attachment.getId());
                safeDeleteS3(s3Key, attachment.getId());
                throw readyEx;
            }

            LOG.info("파일 업로드 성공. attachmentId={}, userId={}, s3Key={}",
                    attachment.getId(), userId, s3Key);
        } catch (CustomException ce) {
            // 위에서 던진 도메인 예외(markReady 실패 등)는 그대로 전달.
            throw ce;
        } catch (Exception e) {
            LOG.error("파일 업로드 실패. attachmentId={}, userId={}, s3Key={}",
                    attachment.getId(), userId, s3Key, e);
            safeMarkFailed(attachment.getId());
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED, e);
        }

        String responseAccessUrl = attachmentUrlService.getAccessUrl(attachment);
        return responseMapper.toUploadResponse(attachment, responseAccessUrl);
    }

    private void safeMarkFailed(Long attachmentId) {
        try {
            attachmentCommandService.markAttachmentAsFailed(attachmentId);
        } catch (Exception ex) {
            LOG.error("FAILED 처리 실패. attachmentId={}", attachmentId, ex);
        }
    }

    private void safeDeleteS3(String s3Key, Long attachmentId) {
        try {
            s3FileUploadService.deleteFile(s3Key);
        } catch (Exception ex) {
            LOG.warn("S3 즉시 삭제 실패. attachmentId={}, s3Key={} (배치에서 정리됨)", attachmentId, s3Key, ex);
        }
    }
}
