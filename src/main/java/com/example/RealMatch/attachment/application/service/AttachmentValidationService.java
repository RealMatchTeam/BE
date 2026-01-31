package com.example.RealMatch.attachment.application.service;

import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.policy.AttachmentUploadPolicy;
import com.example.RealMatch.attachment.application.util.FileValidator;
import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentValidationService {

    private final AttachmentUploadPolicy uploadPolicy;
    private final FileValidator fileValidator;

    public void validateUploadRequest(
            String originalFilename,
            String contentType,
            long fileSize,
            AttachmentType attachmentType
    ) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_NAME);
        }

        fileValidator.validateFileName(originalFilename);

        long maxSize = attachmentType == AttachmentType.IMAGE
                ? uploadPolicy.getMaxImageSizeBytes()
                : uploadPolicy.getMaxFileSizeBytes();
        fileValidator.validateFileSize(fileSize, maxSize);

        if (attachmentType == AttachmentType.IMAGE) {
            fileValidator.validateImageFile(
                    contentType,
                    originalFilename,
                    uploadPolicy.getAllowedImageContentTypes(),
                    uploadPolicy.getAllowedImageExtensions()
            );
        }
    }
}
