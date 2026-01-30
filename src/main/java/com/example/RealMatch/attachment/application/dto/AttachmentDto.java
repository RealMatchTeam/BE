package com.example.RealMatch.attachment.application.dto;

import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;

public record AttachmentDto(
        Long attachmentId,
        AttachmentType attachmentType,
        String contentType,
        String originalName,
        Long fileSize,
        String accessUrl,
        AttachmentStatus status
) {
}
