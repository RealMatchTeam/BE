package com.example.RealMatch.attachment.application.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface AttachmentCommandService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void markAttachmentAsFailed(Long attachmentId);
}
