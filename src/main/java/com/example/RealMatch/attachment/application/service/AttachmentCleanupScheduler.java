package com.example.RealMatch.attachment.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.RealMatch.attachment.domain.repository.AttachmentRepository.AttachmentCleanupTarget;
import com.example.RealMatch.attachment.infrastructure.storage.S3FileUploadService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttachmentCleanupScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentCleanupScheduler.class);

    private final AttachmentCleanupService cleanupService;
    private final Optional<S3FileUploadService> s3FileUploadService;

    @Value("${app.attachment.cleanup.retention-days:7}")
    private int retentionDays;

    @Value("${app.attachment.cleanup.pending-retention-minutes:30}")
    private int pendingRetentionMinutes;

    @Value("${app.attachment.cleanup.batch-size:200}")
    private int batchSize;

    @Scheduled(cron = "${app.attachment.cleanup.cron:0 0 3 * * *}")
    public void cleanupFailedAttachments() {
        if (retentionDays <= 0 && pendingRetentionMinutes <= 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (pendingRetentionMinutes > 0) {
            LocalDateTime pendingCutoff = now.minusMinutes(pendingRetentionMinutes);
            CleanupSummary pendingSummary = processPendingFailures(pendingCutoff);
            if (pendingSummary.claimedCount > 0 || pendingSummary.deleteFailed > 0) {
                LOG.info("Attachment pending cleanup completed. pendingRetentionMinutes={}, claimed={}, deleteFailed={}",
                        pendingRetentionMinutes, pendingSummary.claimedCount, pendingSummary.deleteFailed);
            }
        }

        if (retentionDays > 0) {
            LocalDateTime cutoff = now.minusDays(retentionDays);
            CleanupSummary retentionSummary = processFailedRetention(cutoff);
            if (retentionSummary.softDeleted > 0 || retentionSummary.deleteFailed > 0) {
                LOG.info("Attachment cleanup completed. retentionDays={}, softDeleted={}, deleteFailed={}",
                        retentionDays, retentionSummary.softDeleted, retentionSummary.deleteFailed);
            }
        }
    }

    private CleanupSummary processPendingFailures(LocalDateTime cutoff) {
        CleanupSummary summary = new CleanupSummary();
        while (true) {
            List<AttachmentCleanupTarget> targets = cleanupService.claimStaleUploaded(cutoff, batchSize);
            if (targets.isEmpty()) {
                break;
            }
            summary.claimedCount += targets.size();
            DeleteOutcome deleteOutcome = deleteStorageTargets(targets);
            summary.deleteFailed += deleteOutcome.failedIds.size();
            cleanupService.markDeletePendingAsFailed(extractIds(targets));
        }
        return summary;
    }

    private CleanupSummary processFailedRetention(LocalDateTime cutoff) {
        CleanupSummary summary = new CleanupSummary();
        while (true) {
            List<AttachmentCleanupTarget> targets = cleanupService.claimFailedForRetention(cutoff, batchSize);
            if (targets.isEmpty()) {
                break;
            }
            summary.claimedCount += targets.size();
            DeleteOutcome deleteOutcome = deleteStorageTargets(targets);
            if (!deleteOutcome.successIds.isEmpty()) {
                cleanupService.softDeleteByIds(deleteOutcome.successIds, LocalDateTime.now());
                summary.softDeleted += deleteOutcome.successIds.size();
            }
            if (!deleteOutcome.failedIds.isEmpty()) {
                cleanupService.revertDeletePendingToFailed(deleteOutcome.failedIds);
                summary.deleteFailed += deleteOutcome.failedIds.size();
            }
        }
        return summary;
    }

    private DeleteOutcome deleteStorageTargets(List<AttachmentCleanupTarget> targets) {
        DeleteOutcome outcome = new DeleteOutcome();
        if (targets == null || targets.isEmpty()) {
            return outcome;
        }
        if (s3FileUploadService.isEmpty() || !s3FileUploadService.get().isAvailable()) {
            outcome.successIds.addAll(extractIds(targets));
            return outcome;
        }
        for (AttachmentCleanupTarget target : targets) {
            if (target == null) {
                continue;
            }
            String storageKey = target.getStorageKey();
            if (storageKey == null || storageKey.isBlank()) {
                LOG.warn("Attachment storageKey missing. attachmentId={}", target.getId());
                outcome.successIds.add(target.getId());
                continue;
            }
            try {
                s3FileUploadService.get().deleteFile(storageKey);
                outcome.successIds.add(target.getId());
            } catch (Exception ex) {
                LOG.error("Attachment storage delete failed. attachmentId={}, storageKey={}",
                        target.getId(), storageKey, ex);
                outcome.failedIds.add(target.getId());
            }
        }
        return outcome;
    }

    private List<Long> extractIds(List<AttachmentCleanupTarget> targets) {
        if (targets == null || targets.isEmpty()) {
            return List.of();
        }
        return targets.stream()
                .map(AttachmentCleanupTarget::getId)
                .collect(Collectors.toList());
    }

    private static class DeleteOutcome {
        private final List<Long> successIds = new ArrayList<>();
        private final List<Long> failedIds = new ArrayList<>();
    }

    private static class CleanupSummary {
        private int claimedCount;
        private int softDeleted;
        private int deleteFailed;
    }
}
