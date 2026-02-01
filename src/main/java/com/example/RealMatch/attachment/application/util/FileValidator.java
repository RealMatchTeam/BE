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
        validateFileFormat(
                contentType, filename,
                allowedContentTypes, allowedExtensions,
                AttachmentErrorCode.INVALID_IMAGE_TYPE
        );
    }

    public void validateAttachmentFile(
            String contentType,
            String filename,
            Set<String> allowedContentTypes,
            Set<String> allowedExtensions
    ) {
        validateFileFormat(
                contentType, filename,
                allowedContentTypes, allowedExtensions,
                AttachmentErrorCode.INVALID_FILE_TYPE
        );
    }

    private void validateFileFormat(
            String contentType,
            String filename,
            Set<String> allowedContentTypes,
            Set<String> allowedExtensions,
            AttachmentErrorCode errorCode
    ) {
        Set<String> contentTypes = allowedContentTypes == null ? Set.of() : allowedContentTypes;
        String mainContentType = getMainContentType(contentType);
        if (mainContentType == null || !contentTypes.contains(mainContentType)) {
            throw new CustomException(errorCode);
        }

        if (filename != null && !filename.isBlank()) {
            String extension = getFileExtension(filename).toLowerCase();
            Set<String> extensions = allowedExtensions == null ? Set.of() : allowedExtensions;
            if (!extensions.contains(extension)) {
                throw new CustomException(errorCode);
            }
        }
    }

    private String getMainContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        int semicolon = contentType.indexOf(';');
        String main = semicolon < 0 ? contentType.trim() : contentType.substring(0, semicolon).trim();
        return main.isEmpty() ? null : main.toLowerCase(java.util.Locale.ROOT);
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
