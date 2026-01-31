package com.example.RealMatch.attachment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.repository.AttachmentRepository;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentCommandServiceImpl implements AttachmentCommandService {

    private final AttachmentRepository attachmentRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAttachmentAsFailed(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
        attachment.markAsFailed();
    }
}
