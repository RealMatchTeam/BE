package com.example.RealMatch.attachment.application.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.dto.AttachmentDto;
import com.example.RealMatch.attachment.application.mapper.AttachmentResponseMapper;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.exception.AttachmentException;
import com.example.RealMatch.attachment.domain.repository.AttachmentRepository;
import com.example.RealMatch.attachment.presentation.code.AttachmentErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentQueryServiceImpl implements AttachmentQueryService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentResponseMapper responseMapper;

    @Override
    public AttachmentDto findById(Long attachmentId) {
        if (attachmentId == null) {
            return null;
        }
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElse(null);
        return attachment != null ? responseMapper.toDto(attachment) : null;
    }

    @Override
    public Map<Long, AttachmentDto> findAllById(List<Long> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return Map.of();
        }
        return attachmentRepository.findAllById(attachmentIds).stream()
                .collect(Collectors.toMap(
                        Attachment::getId,
                        responseMapper::toDto
                ));
    }

    @Override
    public void validateOwnership(Long attachmentId, Long userId) {
        if (attachmentId == null) {
            return;
        }
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new AttachmentException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
        
        if (!attachment.getUploaderId().equals(userId)) {
            throw new AttachmentException(AttachmentErrorCode.ATTACHMENT_OWNERSHIP_MISMATCH);
        }
    }
}
