package com.example.RealMatch.attachment.application.service;

import java.util.List;
import java.util.Map;

import com.example.RealMatch.attachment.presentation.dto.response.AttachmentInfoResponse;

/**
 * 첨부파일 조회를 위한 공통 서비스 인터페이스
 * 모든 도메인에서 첨부파일을 조회할 때 사용하는 공통 인터페이스입니다.
 */
public interface AttachmentQueryService {

    AttachmentInfoResponse findById(Long attachmentId);

    Map<Long, AttachmentInfoResponse> findAllById(List<Long> attachmentIds);

    void validateOwnership(Long attachmentId, Long userId);
}
