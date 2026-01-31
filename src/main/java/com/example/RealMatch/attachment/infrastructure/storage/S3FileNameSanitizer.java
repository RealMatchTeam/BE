package com.example.RealMatch.attachment.infrastructure.storage;

import org.springframework.stereotype.Component;

@Component
public class S3FileNameSanitizer {

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
}
