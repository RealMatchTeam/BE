package com.example.RealMatch.attachment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.RealMatch.attachment.application.dto.request.AttachmentUploadRequest;
import com.example.RealMatch.attachment.application.mapper.AttachmentResponseMapper;
import com.example.RealMatch.attachment.application.port.AttachmentStorage;
import com.example.RealMatch.attachment.application.repository.AttachmentRepository;
import com.example.RealMatch.attachment.application.service.AttachmentCleanupScheduler;
import com.example.RealMatch.attachment.application.service.AttachmentCleanupService;
import com.example.RealMatch.attachment.application.service.AttachmentCommandService;
import com.example.RealMatch.attachment.application.service.AttachmentQueryService;
import com.example.RealMatch.attachment.application.service.AttachmentService;
import com.example.RealMatch.attachment.application.service.AttachmentUploadTxService;
import com.example.RealMatch.attachment.application.service.AttachmentUrlService;
import com.example.RealMatch.attachment.application.service.AttachmentValidationService;
import com.example.RealMatch.attachment.application.util.FileValidator;
import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentStatus;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;
import com.example.RealMatch.attachment.infrastructure.storage.S3AttachmentUploadPolicy;
import com.example.RealMatch.attachment.infrastructure.storage.S3FileNameSanitizer;
import com.example.RealMatch.attachment.infrastructure.storage.S3FileUploadServiceImpl;
import com.example.RealMatch.attachment.infrastructure.storage.S3Properties;
import com.example.RealMatch.global.exception.CustomException;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class AttachmentReliabilityTest {
    private final LocalDateTime now = LocalDateTime.now();
    private final AttachmentRepository repository = mock(AttachmentRepository.class);
    private final AttachmentStorage storage = mock(AttachmentStorage.class);

    private Attachment attachment(AttachmentUsage usage) {
        var value = Attachment.createUploading(1L, AttachmentType.IMAGE, "image/png", "file.png", 20L, usage);
        value.setStorageKey("attachment/file.png");
        ReflectionTestUtils.setField(value, "id", 1L);
        ReflectionTestUtils.setField(value, "createdAt", now.minusDays(2));
        return value;
    }

    @ParameterizedTest
    @ValueSource(strings = {"storage", "commit", "url"})
    void uploadFailuresPreserveCommittedReadyAndNeverBlindlyDeleteObject(String failureStage) throws Exception {
        var value = attachment(AttachmentUsage.CHAT);
        var upload = mock(AttachmentUploadTxService.class);
        var validation = mock(AttachmentValidationService.class);
        var urls = mock(AttachmentUrlService.class);
        var staged = Files.createTempFile("upload-failure-test-", ".upload");
        when(validation.validateUploadRequest(anyString(), anyString(), anyLong(), any())).thenReturn("image/png");
        when(validation.stage(any(), anyString(), anyString(), anyLong())).thenReturn(staged);
        when(repository.findForUpdate(1L)).thenReturn(Optional.of(value));
        when(storage.isAvailable()).thenReturn(true);
        when(upload.createAttachmentAndSetStorageKey(anyLong(), any(), anyString(), anyString(), anyLong(), any()))
                .thenReturn(new AttachmentUploadTxService.CreateResult(value, value.getStorageKey()));
        if (failureStage.equals("storage")) {
            doThrow(new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED))
                    .when(storage).uploadFile(any(), anyString(), anyString(), anyLong(), any());
        } else {
            when(upload.markAttachmentAsReady(1L)).thenAnswer(invocation -> {
                value.ready();
                if (failureStage.equals("commit")) {
                    throw new IllegalStateException("Commit response lost after READY persisted");
                }
                return value;
            });
            when(urls.getAccessUrl(value)).thenThrow(new CustomException(AttachmentErrorCode.STORAGE_UNAVAILABLE));
        }
        var service = new AttachmentService(upload, storage, new AttachmentCommandService(repository),
                validation, urls, new AttachmentResponseMapper());
        try {
            assertThrows(CustomException.class, () -> service.uploadAttachment(1L,
                    new AttachmentUploadRequest(AttachmentType.IMAGE, AttachmentUsage.CHAT),
                    new ByteArrayInputStream(new byte[0]), "file.png", "image/png", 20L));
            assertEquals(failureStage.equals("storage") ? AttachmentStatus.FAILED : AttachmentStatus.READY, value.getStatus());
            verify(storage, never()).deleteFile(anyString(), any());
            assertFalse(Files.exists(staged));
        } finally {
            Files.deleteIfExists(staged);
        }
    }

    @Test
    void failedDeleteReturnsWithBackoffRatherThanLooping() {
        var value = attachment(AttachmentUsage.CHAT);
        value.failUpload(now);
        when(repository.findCleanupCandidates(any(), any())).thenReturn(List.of(1L));
        when(repository.findForUpdate(1L)).thenReturn(Optional.of(value));
        when(storage.isAvailable()).thenReturn(true);
        doThrow(new IllegalStateException("S3 down")).when(storage).deleteFile(anyString(), any());
        new AttachmentCleanupScheduler(new AttachmentCleanupService(repository), storage, new SimpleMeterRegistry()).cleanupFailedAttachments();
        verify(storage).deleteFile("attachment/file.png", AttachmentUsage.CHAT);
        assertEquals(AttachmentStatus.FAILED, value.getStatus());
        assertTrue(value.getNextCleanupAt().isAfter(now));
        assertFalse(value.isDeleted());
    }

    @Test
    void missingStorageDoesNotClaimOrDelete() {
        var cleanup = mock(AttachmentCleanupService.class);
        new AttachmentCleanupScheduler(cleanup, storage, new SimpleMeterRegistry()).cleanupFailedAttachments();
        verifyNoInteractions(cleanup);
    }

    @Test
    void expiredDeleteClaimCanRecoverButOldOwnerCannotComplete() {
        var value = attachment(AttachmentUsage.CHAT);
        var old = value.claimCleanup(now.minusMinutes(11));
        assertTrue(value.cleanupDue(now));
        var current = value.claimCleanup(now);
        value.finishCleanup(old, true, now);
        assertFalse(value.isDeleted());
        value.finishCleanup(current, true, now);
        assertTrue(value.isDeleted());
    }

    @Test
    void repeatedCrashesAlsoExhaustTheCleanupBudget() {
        var value = attachment(AttachmentUsage.CHAT);
        for (int i = 0; i < 10; i++) {
            assertNotNull(value.claimCleanup(now.plusMinutes(11L * i)));
        }
        assertNull(value.claimCleanup(now.plusDays(1)));
        assertEquals(AttachmentStatus.DELETE_FAILED, value.getStatus());
    }

    @Test
    void repeatedDeleteFailuresAreQuarantined() {
        var value = attachment(AttachmentUsage.CHAT);
        for (int i = 0; i < 10; i++) {
            value.finishCleanup(value.claimCleanup(now), false, now);
        }
        assertEquals(AttachmentStatus.DELETE_FAILED, value.getStatus());
        assertFalse(value.cleanupDue(now.plusYears(1)));
    }

    @Test
    void retainedReadyFileSurvivesAndAmbiguousReadyCannotBecomeFailed() {
        var value = attachment(AttachmentUsage.CHAT);
        value.ready();
        assertTrue(value.cleanupDue(now.plusDays(2)));
        value.retain(now);
        value.failUpload(now);
        assertEquals(AttachmentStatus.READY, value.getStatus());
        assertFalse(value.cleanupDue(now));
    }

    @Test
    void legacyReadyWithoutDeadlineIsPreservedAndKeysAreCaseSensitive() {
        var value = attachment(AttachmentUsage.PUBLIC);
        value.ready();
        ReflectionTestUtils.setField(value, "nextCleanupAt", null);
        assertFalse(value.cleanupDue(now.plusYears(10)));
        assertFalse(Attachment.hashStorageKey("attachment/A.png").equals(Attachment.hashStorageKey("attachment/a.png")));
        assertEquals(64, value.getStorageKeyHash().length());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUBLIC", "CHAT"})
    void chatRetentionChecksOwnerUsageAndType(String usage) {
        var value = attachment(AttachmentUsage.valueOf(usage));
        value.ready();
        when(repository.findForUpdate(1L)).thenReturn(Optional.of(value));
        var service = new AttachmentQueryService(repository, new AttachmentResponseMapper(), mock(AttachmentUrlService.class), storage);
        assertThrows(CustomException.class, () -> service.retainForChat(1L, 2L, AttachmentType.IMAGE));
        assertThrows(CustomException.class, () -> service.retainForChat(1L, 1L, AttachmentType.FILE));
        if ("PUBLIC".equals(usage)) {
            assertThrows(CustomException.class, () -> service.retainForChat(1L, 1L, AttachmentType.IMAGE));
        } else {
            service.retainForChat(1L, 1L, AttachmentType.IMAGE); assertNotNull(value.getRetainedAt());
        }
    }

    @Test
    void bytesAndMetadataMustAgreeAndStagingIsBounded() throws Exception {
        var validator = new AttachmentValidationService(new S3AttachmentUploadPolicy(new S3Properties()), new FileValidator());
        byte[] png;
        try (var output = new java.io.ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB), "png", output);
            png = output.toByteArray();
        }
        var path = validator.stage(new ByteArrayInputStream(png), "image.png", "image/png", png.length);
        try {
            assertEquals(png.length, Files.size(path));
        } finally {
            Files.delete(path);
        }
        assertThrows(CustomException.class, () -> validator.stage(new ByteArrayInputStream(png), "image.jpg", "image/jpeg", png.length));
        assertThrows(CustomException.class, () -> validator.stage(new ByteArrayInputStream(png), "image.png", "image/jpeg", png.length));
        assertThrows(CustomException.class, () -> validator.stage(new ByteArrayInputStream(png), "image.png", "image/png", png.length - 1));
        assertThrows(CustomException.class, () -> validator.stage(new ByteArrayInputStream(png), "image.png", "image/png", png.length + 1));
    }

    @Test
    void localMinioPublicUrlIsAcceptedWithoutTrustingRemoteHttpUrls() {
        var properties = new S3Properties();
        properties.setCloudfrontBaseUrl("http://127.0.0.1:9000/realmatch-local-public");
        var service = new S3FileUploadServiceImpl(mock(S3Client.class), mock(S3Presigner.class), properties,
                mock(S3FileNameSanitizer.class));

        assertEquals("attachment/image.png", service.publicStorageKey(
                "http://127.0.0.1:9000/realmatch-local-public/attachment/image.png"));
        assertNull(service.publicStorageKey("http://example.com/realmatch-local-public/attachment/image.png"));
    }
}
