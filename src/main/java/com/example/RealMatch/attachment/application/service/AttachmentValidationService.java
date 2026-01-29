package com.example.RealMatch.attachment.application.service;

import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.util.FileValidator;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.exception.AttachmentException;
import com.example.RealMatch.attachment.infrastructure.storage.S3Properties;
import com.example.RealMatch.attachment.presentation.code.AttachmentErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentValidationService {

    private final S3Properties s3Properties;
    private final FileValidator fileValidator;

    public void validateUploadRequest(
            String originalFilename,
            String contentType,
            long fileSize,
            AttachmentType attachmentType
    ) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_FILE_NAME);
        }

        fileValidator.validateFileName(originalFilename);

        long maxSize = attachmentType == AttachmentType.IMAGE
                ? s3Properties.getMaxImageSizeBytes()
                : s3Properties.getMaxFileSizeBytes();
        fileValidator.validateFileSize(fileSize, maxSize);

        if (attachmentType == AttachmentType.IMAGE) {
            fileValidator.validateImageFile(contentType, originalFilename);
        }
    }
}
