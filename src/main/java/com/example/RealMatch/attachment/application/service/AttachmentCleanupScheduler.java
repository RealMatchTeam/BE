package com.example.RealMatch.attachment.application.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.RealMatch.attachment.application.port.AttachmentStorage;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachmentCleanupScheduler {
    private final AttachmentCleanupService cleanupService;
    private final AttachmentStorage storage;
    private final MeterRegistry metrics;
    private final AtomicLong quarantined = new AtomicLong();

    @PostConstruct
    public void registerMetrics() {
        metrics.gauge("attachment.cleanup.quarantined", quarantined);
    }

    @Scheduled(fixedDelayString = "${app.attachment.cleanup.interval-ms:60000}", scheduler = "attachmentScheduler")
    public void cleanupFailedAttachments() {
        if (!storage.isAvailable()) {
            metrics.counter("attachment.cleanup", "result", "unavailable").increment();
            return;
        }
        quarantined.set(cleanupService.quarantinedCount());
        for (var target : cleanupService.claimBatch()) {
            boolean deleted = false;
            try {
                if (target.key() == null || target.key().isBlank() || target.usage() == null) {
                    throw new IllegalStateException("Missing storage identity");
                }
                storage.deleteFile(target.key(), target.usage());
                deleted = true;
            } catch (RuntimeException ex) {
                log.warn("Attachment cleanup failed. attachmentId={}", target.id(), ex);
            }
            metrics.counter("attachment.cleanup", "result", deleted ? "deleted" : "failed").increment();
            try {
                cleanupService.complete(target, deleted);
            } catch (RuntimeException ex) {
                log.error("Attachment cleanup completion failed; lease recovery will retry. attachmentId={}", target.id(), ex);
            }
        }
    }
}
