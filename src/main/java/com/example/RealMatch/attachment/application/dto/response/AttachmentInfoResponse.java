package com.example.RealMatch.attachment.application.dto.response;

import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;

public record AttachmentInfoResponse(
        Long attachmentId,
        AttachmentType attachmentType,
        String contentType,
        String originalName,
        Long fileSize,
        String accessUrl,
        AttachmentStatus status
) {
}
