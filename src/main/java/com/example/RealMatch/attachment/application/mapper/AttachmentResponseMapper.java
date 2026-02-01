package com.example.RealMatch.attachment.application.mapper;

import org.springframework.stereotype.Component;

import com.example.RealMatch.attachment.application.dto.AttachmentDto;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.presentation.dto.response.AttachmentInfoResponse;
import com.example.RealMatch.attachment.presentation.dto.response.AttachmentUploadResponse;

@Component
public class AttachmentResponseMapper {

    public AttachmentDto toDto(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        return new AttachmentDto(
                attachment.getId(),
                attachment.getAttachmentType(),
                attachment.getContentType(),
                attachment.getOriginalName(),
                attachment.getFileSize(),
                null,
                attachment.getStatus()
        );
    }

    public AttachmentUploadResponse toUploadResponse(
            Attachment attachment,
            String accessUrl
    ) {
        if (attachment == null) {
            return null;
        }
        return new AttachmentUploadResponse(
                attachment.getId(),
                attachment.getAttachmentType(),
                attachment.getContentType(),
                attachment.getOriginalName(),
                attachment.getFileSize(),
                accessUrl,
                attachment.getStatus(),
                attachment.getCreatedAt()
        );
    }

    public AttachmentInfoResponse toInfoResponse(AttachmentDto dto) {
        if (dto == null) {
            return null;
        }
        return new AttachmentInfoResponse(
                dto.attachmentId(),
                dto.attachmentType(),
                dto.contentType(),
                dto.originalName(),
                dto.fileSize(),
                dto.accessUrl(),
                dto.status()
        );
    }
}
