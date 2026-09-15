package com.example.RealMatch.attachment.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.repository.AttachmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentCommandService {
    private final AttachmentRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAttachmentAsFailed(Long id) {
        repository.findForUpdate(id).ifPresent(a -> a.failUpload(LocalDateTime.now()));
    }
}
