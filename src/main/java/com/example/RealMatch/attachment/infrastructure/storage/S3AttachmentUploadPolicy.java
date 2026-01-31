package com.example.RealMatch.attachment.infrastructure.storage;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.RealMatch.attachment.application.policy.AttachmentUploadPolicy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class S3AttachmentUploadPolicy implements AttachmentUploadPolicy {

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

    private final S3Properties s3Properties;

    @Override
    public long getMaxImageSizeBytes() {
        return s3Properties.getMaxImageSizeBytes();
    }

    @Override
    public long getMaxFileSizeBytes() {
        return s3Properties.getMaxFileSizeBytes();
    }

    @Override
    public Set<String> getAllowedImageExtensions() {
        return ALLOWED_IMAGE_EXTENSIONS;
    }

    @Override
    public Set<String> getAllowedImageContentTypes() {
        return ALLOWED_IMAGE_CONTENT_TYPES;
    }
}
