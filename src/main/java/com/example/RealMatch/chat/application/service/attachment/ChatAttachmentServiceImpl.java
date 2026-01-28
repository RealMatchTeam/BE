package com.example.RealMatch.chat.application.service.attachment;

import java.io.InputStream;

import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.presentation.dto.request.ChatAttachmentUploadRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatAttachmentUploadResponse;
import com.example.RealMatch.chat.presentation.fixture.ChatFixtureFactory;

@Service
public class ChatAttachmentServiceImpl implements ChatAttachmentService {

    @Override
    public ChatAttachmentUploadResponse uploadAttachment(
            Long userId,
            ChatAttachmentUploadRequest request,
            InputStream fileInputStream,
            String originalFilename,
            String contentType,
            long fileSize
    ) {
        // TODO: 실제 파일 업로드 로직 구현 필요
        // 현재는 Fixture를 사용하지만, 실제 구현 시 fileInputStream을 사용해야 함
        return ChatFixtureFactory.sampleAttachmentUploadResponse(request, originalFilename, contentType, fileSize);
    }
}
