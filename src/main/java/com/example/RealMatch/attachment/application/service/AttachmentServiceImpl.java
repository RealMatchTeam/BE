package com.example.RealMatch.attachment.application.service;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.mapper.AttachmentResponseMapper;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.exception.AttachmentException;
import com.example.RealMatch.attachment.domain.repository.AttachmentRepository;
import com.example.RealMatch.attachment.infrastructure.storage.S3CredentialsCondition;
import com.example.RealMatch.attachment.infrastructure.storage.S3FileUploadService;
import com.example.RealMatch.attachment.presentation.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.presentation.dto.request.AttachmentUploadRequest;
import com.example.RealMatch.attachment.presentation.dto.response.AttachmentUploadResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Conditional(S3CredentialsCondition.class)
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    private final AttachmentRepository attachmentRepository;
    private final S3FileUploadService s3FileUploadService;
    private final AttachmentCommandService attachmentCommandService;
    private final AttachmentValidationService attachmentValidationService;
    private final AttachmentUrlService attachmentUrlService;
    private final AttachmentResponseMapper responseMapper;

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

        // 파일 검증
        attachmentValidationService.validateUploadRequest(
                originalFilename,
                contentType,
                fileSize,
                attachmentType
        );

        // DB에 첨부파일 메타데이터 저장
        Attachment attachment = Attachment.create(
                userId,
                attachmentType,
                contentType,
                originalFilename,
                fileSize,
                null
        );
        attachment = attachmentRepository.save(attachment);

        // S3에 파일 업로드
        String s3Key = null;
        try {
            s3Key = s3FileUploadService.generateS3Key(userId, attachment.getId(), originalFilename);

            String accessUrl = s3FileUploadService.uploadFile(
                    fileInputStream,
                    s3Key,
                    contentType,
                    fileSize
            );

            // S3 업로드 성공 시 accessUrl 업데이트
            if (accessUrl != null) {
                attachment.updateAccessUrl(accessUrl);
            } else {
                attachment.updateAccessUrl(s3Key);
            }

            LOG.info("파일 업로드 성공. attachmentId={}, userId={}, s3Key={}", 
                    attachment.getId(), userId, s3Key);

        } catch (Exception e) {
            LOG.error("파일 업로드 실패. attachmentId={}, userId={}, s3Key={}", 
                    attachment.getId(), userId, s3Key, e);
            
            // 별도 트랜잭션으로 실패 상태 업데이트 시도
            try {
                attachmentCommandService.markAttachmentAsFailed(attachment.getId());
            } catch (Exception markFailedException) {
                LOG.error("실패 상태 업데이트 실패. attachmentId={}", 
                        attachment.getId(), markFailedException);
            }
            
            // 원래 예외를 래핑하여 더 명확한 메시지 제공
            throw new AttachmentException(
                    AttachmentErrorCode.S3_UPLOAD_FAILED,
                    "파일 업로드에 실패했습니다: " + e.getMessage(),
                    e
            );
        }

        // Presigned URL 생성
        String responseAccessUrl = attachmentUrlService.getAccessUrl(attachment);

        // 응답 DTO 생성
        return responseMapper.toUploadResponse(attachment, responseAccessUrl);
    }
}
