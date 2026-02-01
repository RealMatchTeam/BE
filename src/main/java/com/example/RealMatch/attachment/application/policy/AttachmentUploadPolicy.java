package com.example.RealMatch.attachment.application.policy;

import java.util.Set;

public interface AttachmentUploadPolicy {
    long getMaxImageSizeBytes();

    long getMaxFileSizeBytes();

    Set<String> getAllowedImageExtensions();

    Set<String> getAllowedImageContentTypes();

    Set<String> getAllowedFileExtensions();

    Set<String> getAllowedFileContentTypes();
}
