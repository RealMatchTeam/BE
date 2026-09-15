package com.example.RealMatch.attachment.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.port.AttachmentStorage;
import com.example.RealMatch.attachment.application.repository.AttachmentRepository;
import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentUploadTxService {
    private final AttachmentRepository repository;
    private final AttachmentStorage storage;
    private final UserRepository users;

    @Transactional
    public CreateResult createAttachmentAndSetStorageKey(Long userId, AttachmentType type, String contentType,
                                                         String name, long size, AttachmentUsage usage) {
        users.findByIdForUpdate(userId).orElseThrow(() -> new CustomException(AttachmentErrorCode.ATTACHMENT_OWNERSHIP_MISMATCH));
        if (usage == null || repository.countByUploaderIdAndCreatedAtAfter(userId, LocalDateTime.now().minusDays(1)) >= 100) {
            throw new CustomException(AttachmentErrorCode.UPLOAD_LIMIT_EXCEEDED);
        }
        Attachment attachment = Attachment.createUploading(userId, type, contentType, name, size, usage);
        attachment.setStorageKey(storage.generateStorageKey(usage, userId, name));
        repository.save(attachment);
        return new CreateResult(attachment, attachment.getStorageKey());
    }

    @Transactional
    public Attachment markAttachmentAsReady(Long id) {
        Attachment attachment = repository.findForUpdate(id)
                .orElseThrow(() -> new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
        try {
            attachment.ready();
        } catch (IllegalStateException ex) {
            throw new CustomException(AttachmentErrorCode.ATTACHMENT_INVALID_STATUS, ex);
        }
        return attachment;
    }

    public record CreateResult(Attachment attachment, String s3Key) {
    }
}
