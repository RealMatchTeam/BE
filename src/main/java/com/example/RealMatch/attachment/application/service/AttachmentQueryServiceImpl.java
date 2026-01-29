package com.example.RealMatch.attachment.application.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.dto.AttachmentDto;
import com.example.RealMatch.attachment.application.mapper.AttachmentResponseMapper;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.repository.AttachmentRepository;
import com.example.RealMatch.attachment.presentation.code.AttachmentErrorCode;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(AttachmentUrlService.class)
public class AttachmentQueryServiceImpl implements AttachmentQueryService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentResponseMapper responseMapper;
    private final AttachmentUrlService attachmentUrlService;

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
        // Presigned URL 생성
        String presignedUrl = attachmentUrlService.getAccessUrl(attachment);
        if (presignedUrl != null) {
            return new AttachmentDto(
                    dto.attachmentId(),
                    dto.attachmentType(),
                    dto.contentType(),
                    dto.originalName(),
                    dto.fileSize(),
                    presignedUrl,
                    dto.status()
            );
        }
        return dto;
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
                            // Presigned URL 생성
                            String presignedUrl = attachmentUrlService.getAccessUrl(attachment);
                            if (presignedUrl != null) {
                                return new AttachmentDto(
                                        dto.attachmentId(),
                                        dto.attachmentType(),
                                        dto.contentType(),
                                        dto.originalName(),
                                        dto.fileSize(),
                                        presignedUrl,
                                        dto.status()
                                );
                            }
                            return dto;
                        }
                ));
    }

    @Override
    public void validateOwnership(Long attachmentId, Long userId) {
        if (attachmentId == null) {
            return;
        }
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new CustomException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
        
        if (!attachment.getUploaderId().equals(userId)) {
            throw new CustomException(AttachmentErrorCode.ATTACHMENT_OWNERSHIP_MISMATCH);
        }
    }
}
