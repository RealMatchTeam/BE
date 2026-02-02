package com.example.RealMatch.attachment.presentation.controller;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.RealMatch.attachment.application.service.AttachmentService;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;
import com.example.RealMatch.attachment.infrastructure.storage.S3CredentialsCondition;
import com.example.RealMatch.attachment.presentation.dto.request.AttachmentUploadRequest;
import com.example.RealMatch.attachment.presentation.dto.response.AttachmentUploadResponse;
import com.example.RealMatch.attachment.presentation.swagger.AttachmentSwagger;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@Conditional(S3CredentialsCondition.class)
public class AttachmentController implements AttachmentSwagger {

    private final AttachmentService attachmentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public CustomResponse<AttachmentUploadResponse> uploadAttachment(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestParam("attachmentType") AttachmentType attachmentType,
            @Valid @RequestParam("usage") AttachmentUsage usage,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        Long userId = user.getUserId();
        AttachmentUploadRequest request = new AttachmentUploadRequest(attachmentType, usage);
        try (InputStream inputStream = file.getInputStream()) {
            return CustomResponse.ok(attachmentService.uploadAttachment(
                    userId,
                    request,
                    inputStream,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize()
            ));
        }
    }
}
