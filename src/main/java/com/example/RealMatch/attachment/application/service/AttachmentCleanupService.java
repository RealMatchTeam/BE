package com.example.RealMatch.attachment.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.repository.AttachmentRepository;
import com.example.RealMatch.attachment.domain.repository.AttachmentRepository.AttachmentCleanupTarget;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentCleanupService {

    private final AttachmentRepository attachmentRepository;

    @Transactional
    public List<AttachmentCleanupTarget> claimStaleUploaded(LocalDateTime cutoff, int batchSize) {
        return claimTargets(AttachmentStatus.UPLOADED, cutoff, batchSize);
    }

    @Transactional
    public List<AttachmentCleanupTarget> claimFailedForRetention(LocalDateTime cutoff, int batchSize) {
        return claimTargets(AttachmentStatus.FAILED, cutoff, batchSize);
    }

    @Transactional
    public void markDeletePendingAsFailed(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        attachmentRepository.updateStatusByIdsAndStatus(ids, AttachmentStatus.DELETE_PENDING, AttachmentStatus.FAILED);
    }

    @Transactional
    public void softDeleteByIds(List<Long> ids, LocalDateTime deletedAt) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        attachmentRepository.softDeleteByIdsAndStatus(
                ids,
                AttachmentStatus.DELETE_PENDING,
                AttachmentStatus.FAILED,
                deletedAt
        );
    }

    private List<AttachmentCleanupTarget> claimTargets(
            AttachmentStatus fromStatus,
            LocalDateTime cutoff,
            int batchSize
    ) {
        if (cutoff == null || batchSize <= 0) {
            return List.of();
        }
        List<Long> candidateIds = attachmentRepository.findIdsByStatusAndCreatedAtBefore(
                fromStatus,
                cutoff,
                PageRequest.of(0, batchSize, Sort.by("id"))
        );
        if (candidateIds.isEmpty()) {
            return List.of();
        }
        attachmentRepository.updateStatusByIdsAndStatus(
                candidateIds,
                fromStatus,
                AttachmentStatus.DELETE_PENDING
        );
        return attachmentRepository.findCleanupTargetsByIdInAndStatus(
                candidateIds,
                AttachmentStatus.DELETE_PENDING
        );
    }
}
