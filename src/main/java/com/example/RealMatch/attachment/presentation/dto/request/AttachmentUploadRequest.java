package com.example.RealMatch.attachment.presentation.dto.request;

import com.example.RealMatch.attachment.domain.enums.AttachmentType;

import jakarta.validation.constraints.NotNull;

public record AttachmentUploadRequest(
        @NotNull AttachmentType attachmentType
) {
}
