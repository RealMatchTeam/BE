package com.example.RealMatch.attachment.application.service;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.repository.AttachmentRepository;
import com.example.RealMatch.attachment.infrastructure.storage.S3CredentialsCondition;
import com.example.RealMatch.attachment.infrastructure.storage.S3FileUploadService;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Conditional(S3CredentialsCondition.class)
public class AttachmentUploadTxService {

    private final AttachmentRepository attachmentRepository;
    private final S3FileUploadService s3FileUploadService;

    @Transactional
    public CreateResult createAttachmentAndSetStorageKey(
            Long userId,
            AttachmentType attachmentType,
            String contentType,
            String originalFilename,
            long fileSize
    ) {
        Attachment attachment = Attachment.create(
                userId,
                attachmentType,
                contentType,
                originalFilename,
                fileSize,
                null
        );
        attachment = attachmentRepository.save(attachment);

        String s3Key = s3FileUploadService.generateS3Key(userId, attachment.getId(), originalFilename);
        int updated = attachmentRepository.updateStorageKeyIfStatus(
                attachment.getId(),
                AttachmentStatus.UPLOADED,
                s3Key
        );
        if (updated != 1) {
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED);
        }
        return new CreateResult(attachment, s3Key);
    }

    @Transactional
    public Attachment markAttachmentAsReady(Long attachmentId, String accessUrl) {
        int updated = attachmentRepository.updateStatusAndAccessUrlIfStatus(
                attachmentId,
                AttachmentStatus.UPLOADED,
                AttachmentStatus.READY,
                accessUrl
        );
        if (updated != 1) {
            Attachment current = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
            if (current.getStatus() == AttachmentStatus.READY) {
                return current; // 멱등
            }
            // UPLOADED가 아님 (FAILED/DELETE_PENDING 등) → 상태 경합/배치 개입
            throw new CustomException(AttachmentErrorCode.ATTACHMENT_INVALID_STATUS);
        }
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
    }

    public record CreateResult(Attachment attachment, String s3Key) {
    }
}
