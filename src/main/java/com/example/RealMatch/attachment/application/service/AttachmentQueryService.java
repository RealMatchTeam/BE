package com.example.RealMatch.attachment.application.service;

import java.util.List;
import java.util.Map;

import com.example.RealMatch.attachment.application.dto.AttachmentDto;

public interface AttachmentQueryService {

    AttachmentDto findById(Long attachmentId);

    Map<Long, AttachmentDto> findAllById(List<Long> attachmentIds);

    void validateOwnership(Long attachmentId, Long userId);
}
