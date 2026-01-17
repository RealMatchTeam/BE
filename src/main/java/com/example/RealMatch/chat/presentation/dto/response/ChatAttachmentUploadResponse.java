package com.example.RealMatch.chat.presentation.dto.response;

import java.time.LocalDateTime;

import com.example.RealMatch.chat.presentation.dto.enums.ChatAttachmentStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatAttachmentType;

public record ChatAttachmentUploadResponse(
        Long attachmentId,
        ChatAttachmentType attachmentType,
        String contentType,
        String originalName,
        Long fileSize,
        String accessUrl,
        ChatAttachmentStatus status,
        LocalDateTime createdAt
) {
}
