package com.example.RealMatch.attachment.application.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.repository.AttachmentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttachmentCleanupScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentCleanupScheduler.class);

    private final AttachmentRepository attachmentRepository;

    @Value("${app.attachment.cleanup.retention-days:7}")
    private int retentionDays;

    @Scheduled(cron = "${app.attachment.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void cleanupFailedAttachments() {
        if (retentionDays <= 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusDays(retentionDays);
        int affected = attachmentRepository.softDeleteByStatusAndCreatedAtBefore(
                AttachmentStatus.FAILED,
                cutoff,
                now
        );

        if (affected > 0) {
            LOG.info("Attachment cleanup completed. retentionDays={}, softDeleted={}", retentionDays, affected);
        }
    }
}
