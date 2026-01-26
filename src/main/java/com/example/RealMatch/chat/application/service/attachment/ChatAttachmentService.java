package com.example.RealMatch.chat.application.service.attachment;

import java.io.InputStream;

import com.example.RealMatch.chat.presentation.dto.request.ChatAttachmentUploadRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatAttachmentUploadResponse;

public interface ChatAttachmentService {
    ChatAttachmentUploadResponse uploadAttachment(
            Long userId,
            ChatAttachmentUploadRequest request,
            InputStream fileInputStream,
            String originalFilename,
            String contentType,
            long fileSize
    );
}
