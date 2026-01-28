package com.example.RealMatch.chat.presentation.dto.response;

import com.example.RealMatch.chat.domain.enums.ChatAttachmentStatus;
import com.example.RealMatch.chat.domain.enums.ChatAttachmentType;

public record ChatAttachmentInfoResponse(
        Long attachmentId,
        ChatAttachmentType attachmentType,
        String contentType,
        String originalName,
        Long fileSize,
        String accessUrl,
        ChatAttachmentStatus status
) {
}
