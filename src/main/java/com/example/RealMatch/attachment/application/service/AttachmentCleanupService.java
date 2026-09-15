package com.example.RealMatch.attachment.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.repository.AttachmentRepository;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentCleanupService {
    private final AttachmentRepository repository;

    @Transactional(readOnly = true)
    public long quarantinedCount() {
        return repository.countByStatus(AttachmentStatus.DELETE_FAILED);
    }

    @Transactional
    public List<Target> claimBatch() {
        LocalDateTime now = LocalDateTime.now();
        List<Target> targets = new ArrayList<>();
        for (Long id : repository.findCleanupCandidates(now, PageRequest.of(0, 10))) {
            var attachment = repository.findForUpdate(id).orElse(null);
            if (attachment != null && attachment.cleanupDue(now)) {
                UUID token = attachment.claimCleanup(now);
                if (token != null) {
                    targets.add(new Target(id, attachment.getStorageKey(), attachment.getUsage(), token));
                }
            }
        }
        return targets;
    }

    @Transactional
    public void complete(Target target, boolean success) {
        repository.findForUpdate(target.id()).ifPresent(a ->
                a.finishCleanup(target.token(), success, LocalDateTime.now()));
    }

    public record Target(Long id, String key, AttachmentUsage usage, UUID token) {
    }
}
