package com.example.RealMatch.attachment.application.service;

import java.io.InputStream;

import com.example.RealMatch.attachment.presentation.dto.request.AttachmentUploadRequest;
import com.example.RealMatch.attachment.presentation.dto.response.AttachmentUploadResponse;

public interface AttachmentService {
    AttachmentUploadResponse uploadAttachment(
            Long userId,
            AttachmentUploadRequest request,
            InputStream fileInputStream,
            String originalFilename,
            String contentType,
            long fileSize
    );
}
