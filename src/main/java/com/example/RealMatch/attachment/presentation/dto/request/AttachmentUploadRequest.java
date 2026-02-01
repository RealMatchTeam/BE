package com.example.RealMatch.attachment.presentation.dto.request;

import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;

import jakarta.validation.constraints.NotNull;

/** 업로드 API 요청. usage는 S3 경로 분리 및 정책 적용을 위해 필수로 지정해야 합니다. */
public record AttachmentUploadRequest(
        @NotNull AttachmentType attachmentType,
        @NotNull AttachmentUsage usage
) {
}
