package com.example.RealMatch.attachment.application.util;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.global.exception.CustomException;

@Component
public class FileValidator {

    public void validateFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_NAME);
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_NAME);
        }

        if (filename.length() > 255) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_NAME);
        }
    }

    public void validateFileSize(long fileSize, long maxSizeBytes) {
        if (fileSize <= 0) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_SIZE);
        }

        if (fileSize > maxSizeBytes) {
            throw new CustomException(AttachmentErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    public void validateImageFile(
            String contentType,
            String filename,
            Set<String> allowedContentTypes,
            Set<String> allowedExtensions
    ) {
        Set<String> contentTypes = allowedContentTypes == null ? Set.of() : allowedContentTypes;
        if (contentType == null || !contentTypes.contains(contentType.toLowerCase())) {
            throw new CustomException(AttachmentErrorCode.INVALID_IMAGE_TYPE);
        }

        if (filename != null) {
            String extension = getFileExtension(filename).toLowerCase();
            Set<String> extensions = allowedExtensions == null ? Set.of() : allowedExtensions;
            if (!extensions.contains(extension)) {
                throw new CustomException(AttachmentErrorCode.INVALID_IMAGE_TYPE);
            }
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }
}
