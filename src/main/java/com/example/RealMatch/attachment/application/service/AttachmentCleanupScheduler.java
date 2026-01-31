package com.example.RealMatch.attachment.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.repository.AttachmentRepository;
import com.example.RealMatch.attachment.infrastructure.storage.S3FileUploadService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttachmentCleanupScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentCleanupScheduler.class);

    private final AttachmentRepository attachmentRepository;
    private final Optional<S3FileUploadService> s3FileUploadService;

    @Value("${app.attachment.cleanup.retention-days:7}")
    private int retentionDays;

    @Value("${app.attachment.cleanup.pending-retention-minutes:30}")
    private int pendingRetentionMinutes;

    @Scheduled(cron = "${app.attachment.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void cleanupFailedAttachments() {
        if (retentionDays <= 0 && pendingRetentionMinutes <= 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (pendingRetentionMinutes > 0) {
            LocalDateTime pendingCutoff = now.minusMinutes(pendingRetentionMinutes);
            List<Attachment> pendingCandidates = attachmentRepository
                    .findByStatusAndCreatedAtBefore(AttachmentStatus.UPLOADED, pendingCutoff);
            if (!pendingCandidates.isEmpty()) {
                int failedUpdated = 0;
                int deleteFailed = 0;
                int skipped = 0;
                for (Attachment attachment : pendingCandidates) {
                    int updated = attachmentRepository.updateStatusByIdAndStatus(
                            attachment.getId(),
                            AttachmentStatus.UPLOADED,
                            AttachmentStatus.FAILED
                    );
                    if (updated != 1) {
                        skipped++;
                        continue;
                    }
                    if (!deleteStorageIfPossible(attachment)) {
                        deleteFailed++;
                    }
                    failedUpdated++;
                }
                if (failedUpdated > 0) {
                    LOG.info("Attachment pending cleanup completed. pendingRetentionMinutes={}, failedUpdated={}, deleteFailed={}, skipped={}",
                            pendingRetentionMinutes, failedUpdated, deleteFailed, skipped);
                } else if (skipped > 0) {
                    LOG.info("Attachment pending cleanup skipped. pendingRetentionMinutes={}, skipped={}",
                            pendingRetentionMinutes, skipped);
                }
            }
        }

        if (retentionDays > 0) {
            LocalDateTime cutoff = now.minusDays(retentionDays);
            List<Attachment> candidates = attachmentRepository
                    .findByStatusAndCreatedAtBefore(AttachmentStatus.FAILED, cutoff);
            if (!candidates.isEmpty()) {
                int softDeleted = 0;
                for (Attachment attachment : candidates) {
                    if (deleteStorageIfPossible(attachment)) {
                        attachment.softDelete();
                        softDeleted++;
                    }
                }
                if (softDeleted > 0) {
                    LOG.info("Attachment cleanup completed. retentionDays={}, softDeleted={}", retentionDays, softDeleted);
                }
            }
        }
    }

    private boolean deleteStorageIfPossible(Attachment attachment) {
        if (attachment == null) {
            return false;
        }
        if (s3FileUploadService.isEmpty()) {
            return true;
        }
        String storageKey = attachment.getStorageKey();
        if (storageKey == null || storageKey.isBlank()) {
            LOG.warn("Attachment storageKey missing. attachmentId={}", attachment.getId());
            return true;
        }
        try {
            s3FileUploadService.get().deleteFile(storageKey);
            return true;
        } catch (Exception ex) {
            LOG.error("Attachment storage delete failed. attachmentId={}, storageKey={}",
                    attachment.getId(), storageKey, ex);
            return false;
        }
    }
}
