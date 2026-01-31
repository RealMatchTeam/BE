package com.example.RealMatch.attachment.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.dto.AttachmentDto;
import com.example.RealMatch.attachment.application.mapper.AttachmentResponseMapper;
import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.repository.AttachmentRepository;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentQueryServiceImpl implements AttachmentQueryService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentResponseMapper responseMapper;
    private final Optional<AttachmentUrlService> attachmentUrlService;

    @Override
    public AttachmentDto findById(Long attachmentId) {
        if (attachmentId == null) {
            return null;
        }
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElse(null);
        if (attachment == null) {
            return null;
        }
        AttachmentDto dto = responseMapper.toDto(attachment);
        return enrichWithPresignedUrl(dto, attachment);
    }

    @Override
    public AttachmentDto findByIdOrThrow(Long attachmentId) {
        if (attachmentId == null) {
            throw new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND);
        }
        Attachment attachment = getAttachmentOrThrow(attachmentId);
        AttachmentDto dto = responseMapper.toDto(attachment);
        return enrichWithPresignedUrl(dto, attachment);
    }

    @Override
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
                        }
                ));
    }

    @Override
    public void validateOwnership(Long attachmentId, Long userId) {
        if (attachmentId == null) {
            return;
        }
        Attachment attachment = getAttachmentOrThrow(attachmentId);
        
        if (!attachment.getUploaderId().equals(userId)) {
            throw new CustomException(AttachmentErrorCode.ATTACHMENT_OWNERSHIP_MISMATCH);
        }
    }

    private Attachment getAttachmentOrThrow(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
    }

    private AttachmentDto enrichWithPresignedUrl(AttachmentDto dto, Attachment attachment) {
        if (dto == null || attachment == null) {
            return dto;
        }
        return attachmentUrlService
                .map(service -> service.getAccessUrl(attachment))
                .map(presignedUrl -> new AttachmentDto(
                        dto.attachmentId(),
                        dto.attachmentType(),
                        dto.contentType(),
                        dto.originalName(),
                        dto.fileSize(),
                        presignedUrl,
                        dto.status()
                ))
                .orElse(dto);
    }
}
