package com.example.RealMatch.attachment.application.util;

import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.exception.AttachmentException;
import com.example.RealMatch.attachment.presentation.code.AttachmentErrorCode;

@Component
public class FileValidator {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    public void validateFile(
            MultipartFile file,
            AttachmentType attachmentType,
            long maxSizeBytes
    ) {
        if (file == null || file.isEmpty()) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_FILE);
        }

        validateFileName(file.getOriginalFilename());
        validateFileSize(file.getSize(), maxSizeBytes);

        if (attachmentType == AttachmentType.IMAGE) {
            validateImageFile(file);
        }
    }

    public void validateFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_FILE_NAME);
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_FILE_NAME);
        }

        if (filename.length() > 255) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_FILE_NAME);
        }
    }

    public void validateFileSize(long fileSize, long maxSizeBytes) {
        if (fileSize <= 0) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_FILE_SIZE);
        }

        if (fileSize > maxSizeBytes) {
            throw new AttachmentException(AttachmentErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    public void validateImageFile(MultipartFile file) {
        validateImageFile(file.getContentType(), file.getOriginalFilename());
    }

    public void validateImageFile(String contentType, String filename) {
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_IMAGE_TYPE);
        }

        if (filename != null) {
            String extension = getFileExtension(filename).toLowerCase();
            if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
                throw new AttachmentException(AttachmentErrorCode.INVALID_IMAGE_TYPE);
            }
        }
    }

    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }

    public String sanitizeFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }

        String sanitized = filename
                .replace("..", "")
                .replace("/", "_")
                .replace("\\", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "_");

        if (sanitized.isBlank() || sanitized.length() < 3) {
            sanitized = "file_" + System.currentTimeMillis();
        }

        return sanitized;
    }
}
