package com.example.RealMatch.chat.presentation.dto.request;

import com.example.RealMatch.chat.presentation.dto.enums.ChatAttachmentType;

import jakarta.validation.constraints.NotNull;

public record ChatAttachmentUploadRequest(
        @NotNull ChatAttachmentType attachmentType
) {
}
