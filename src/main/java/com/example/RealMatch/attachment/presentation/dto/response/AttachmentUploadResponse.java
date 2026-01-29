package com.example.RealMatch.attachment.presentation.dto.response;

import java.time.LocalDateTime;

import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;

public record AttachmentUploadResponse(
        Long attachmentId,
        AttachmentType attachmentType,
        String contentType,
        String originalName,
        Long fileSize,
        String accessUrl,
        AttachmentStatus status,
        LocalDateTime createdAt
) {
}
