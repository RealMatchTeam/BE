package com.example.RealMatch.chat.application.service.attachment;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.RealMatch.chat.presentation.dto.request.ChatAttachmentUploadRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatAttachmentUploadResponse;
import com.example.RealMatch.chat.presentation.fixture.ChatFixtureFactory;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;

@Service
public class ChatAttachmentServiceImpl implements ChatAttachmentService {

    @Override
    public ChatAttachmentUploadResponse uploadAttachment(
            CustomUserDetails user,
            ChatAttachmentUploadRequest request,
            MultipartFile file
    ) {
        return ChatFixtureFactory.sampleAttachmentUploadResponse(request, file);
    }
}
