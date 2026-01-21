package com.example.RealMatch.chat.application.service.attachment;

import org.springframework.web.multipart.MultipartFile;

import com.example.RealMatch.chat.presentation.dto.request.ChatAttachmentUploadRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatAttachmentUploadResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;

public interface ChatAttachmentService {
    ChatAttachmentUploadResponse uploadAttachment(
            CustomUserDetails user,
            ChatAttachmentUploadRequest request,
            MultipartFile file
    );
}
