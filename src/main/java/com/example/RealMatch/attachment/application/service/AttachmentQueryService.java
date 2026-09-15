package com.example.RealMatch.attachment.application.service;

import static com.example.RealMatch.attachment.domain.enums.AttachmentUsage.CHAT;
import static com.example.RealMatch.attachment.domain.enums.AttachmentUsage.PUBLIC;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.dto.AttachmentDto;
import com.example.RealMatch.attachment.application.mapper.AttachmentResponseMapper;
import com.example.RealMatch.attachment.application.port.AttachmentStorage;
import com.example.RealMatch.attachment.application.repository.AttachmentRepository;
import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentQueryService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentResponseMapper responseMapper;
    private final AttachmentUrlService attachmentUrlService;
    private final AttachmentStorage storage;

    public Map<Long, AttachmentDto> findAllById(List<Long> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return Map.of();
        }
        return attachmentRepository.findAllById(attachmentIds).stream()
                .collect(Collectors.toMap(
                        Attachment::getId,
                        attachment -> {
                            AttachmentDto dto = responseMapper.toDto(attachment);
                            return enrichWithPresignedUrl(dto, attachment);
                        }));
    }

    private AttachmentDto enrichWithPresignedUrl(AttachmentDto dto, Attachment attachment) {
        if (dto == null || attachment == null) {
            return dto;
        }
        return new AttachmentDto(dto.attachmentId(), dto.attachmentType(), dto.contentType(), dto.originalName(),
                dto.fileSize(), attachmentUrlService.getAccessUrl(attachment), dto.status());
    }

    @Transactional
    public AttachmentDto retainForChat(Long attachmentId, Long userId, AttachmentType type) {
        var attachment = attachmentRepository.findForUpdate(attachmentId)
                .orElseThrow(() -> new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
        if (!attachment.getUploaderId().equals(userId)) {
            throw new CustomException(AttachmentErrorCode.ATTACHMENT_OWNERSHIP_MISMATCH);
        }
        if (attachment.getStatus() != AttachmentStatus.READY) {
            throw new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_READY);
        }
        if (attachment.getUsage() != CHAT || attachment.getAttachmentType() != type) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_TYPE);
        }
        attachment.retain(LocalDateTime.now());
        return enrichWithPresignedUrl(responseMapper.toDto(attachment), attachment);
    }

    @Transactional
    public void retainPublicUrl(Long userId, String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String key = storage.publicStorageKey(url);
        if (key == null) {
            return;
        }
        var found = attachmentRepository.findByStorageKey(key)
                .orElseThrow(() -> new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
        var attachment = attachmentRepository.findForUpdate(found.getId()).orElseThrow();
        if (!attachment.getUploaderId().equals(userId)) {
            throw new CustomException(AttachmentErrorCode.ATTACHMENT_OWNERSHIP_MISMATCH);
        }
        if (attachment.getStatus() != AttachmentStatus.READY) {
            throw new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_READY);
        }
        if (attachment.getUsage() != PUBLIC) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_TYPE);
        }
        attachment.retain(LocalDateTime.now());
    }
}
