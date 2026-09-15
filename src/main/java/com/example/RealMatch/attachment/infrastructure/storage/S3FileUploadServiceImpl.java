package com.example.RealMatch.attachment.infrastructure.storage;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.port.AttachmentStorage;
import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@RequiredArgsConstructor
@Conditional(S3CredentialsCondition.class)
public class S3FileUploadServiceImpl implements AttachmentStorage {

    private static final Logger LOG = LoggerFactory.getLogger(S3FileUploadServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;
    private final S3FileNameSanitizer fileNameSanitizer;

    @Override
    public void uploadFile(InputStream inputStream, String key, String contentType, long fileSize, AttachmentUsage usage) {
        String bucket = resolveBucket(usage);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(fileSize)
                    .contentDisposition(contentType.startsWith("image/") ? "inline" : "attachment")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, fileSize));
            LOG.info("S3 업로드 완료. bucket={}, key={}, usage={}", bucket, key, usage);
        } catch (S3Exception e) {
            handleS3Exception("파일 업로드", key, bucket, e);
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED, e);
        } catch (Exception e) {
            LOG.error("S3 파일 업로드 중 예상치 못한 오류 발생. bucket={}, key={}", bucket, key, e);
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED, e);
        }
    }

    @Override
    public String generatePresignedUrl(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                    presigner -> presigner
                            .signatureDuration(Duration.ofSeconds(Math.max(30, Math.min(900, s3Properties.getPresignedUrlExpirationSeconds()))))
                            .getObjectRequest(getObjectRequest)
            );

            return presignedRequest.url().toString();
        } catch (S3Exception e) {
            handleS3Exception("Presigned URL 생성", key, s3Properties.getBucketName(), e);
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED, e);
        } catch (Exception e) {
            LOG.error("Presigned URL 생성 중 예상치 못한 오류 발생. key={}", key, e);
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED, e);
        }
    }

    @Override
    public String generateStorageKey(AttachmentUsage usage, Long userId, String originalFilename) {
        String sanitizedFilename = fileNameSanitizer.sanitizeFileName(originalFilename);
        String extension = fileNameSanitizer.getFileExtension(originalFilename);
        String filename = sanitizedFilename;

        if (!extension.isEmpty() && !filename.toLowerCase().endsWith("." + extension.toLowerCase())) {
            filename = filename + "." + extension;
        }

        String uuid = UUID.randomUUID().toString();
        String uniqueFilename = uuid + "_" + filename;
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String usagePrefix = usage.name().toLowerCase();
        return String.format("%s/%s/%d/%s/%s",
                s3Properties.getKeyPrefix(), usagePrefix, userId, datePath, uniqueFilename);
    }

    @Override
    public void deleteFile(String key, AttachmentUsage usage) {
        String bucket = resolveBucket(usage);
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            handleS3Exception("파일 삭제", key, bucket, e);
            throw new CustomException(AttachmentErrorCode.S3_DELETE_FAILED, e);
        } catch (Exception e) {
            LOG.error("S3 파일 삭제 중 예상치 못한 오류 발생. bucket={}, key={}", bucket, key, e);
            throw new CustomException(AttachmentErrorCode.S3_DELETE_FAILED, e);
        }
    }

    @Override
    public String buildPublicUrl(String storageKey) {
        String base = s3Properties.getCloudfrontBaseUrl();
        if (base == null || base.isBlank()) {
            LOG.warn("CloudFront base URL이 설정되지 않았습니다. storageKey={}", storageKey);
            return null;
        }
        if (storageKey != null && storageKey.contains("..")) {
            LOG.warn("storageKey contains path traversal sequence. storageKey={}", storageKey);
            return null;
        }
        return base.endsWith("/") ? base + storageKey : base + "/" + storageKey;
    }

    @Override
    public String publicStorageKey(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (!value.contains("://")) {
            return value;
        }
        String baseUrl = s3Properties.getCloudfrontBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        try {
            var base = URI.create(baseUrl);
            var url = URI.create(value);
            if (!allowedScheme(base, url) || !Objects.equals(base.getHost(), url.getHost())
                    || base.getPort() != url.getPort() || url.getUserInfo() != null || url.getQuery() != null
                    || url.getFragment() != null || !url.normalize().equals(url)) {
                return null;
            }
            String prefix = base.getPath().replaceAll("/+$", "") + "/";
            return url.getPath().startsWith(prefix) ? url.getPath().substring(prefix.length()) : null;
        } catch (IllegalArgumentException ex) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE, ex);
        }
    }

    private boolean allowedScheme(URI base, URI url) {
        if (!Objects.equals(base.getScheme(), url.getScheme())) {
            return false;
        }
        if ("https".equals(url.getScheme())) {
            return true;
        }
        String host = base.getHost();
        return "http".equals(url.getScheme())
                && ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "::1".equals(host));
    }

    /**
     * usage에 따라 대상 버킷 결정.
     * PUBLIC → publicBucketName, CHAT → bucketName (private).
     */
    private String resolveBucket(AttachmentUsage usage) {
        if (usage == null) {
            throw new IllegalArgumentException("Attachment usage required");
        }
        if (usage == AttachmentUsage.PUBLIC) {
            String publicBucket = s3Properties.getPublicBucketName();
            if (publicBucket == null || publicBucket.isBlank()) {
                LOG.error("PUBLIC usage attachment requires a public bucket, but 'app.s3.public-bucket-name' is not configured.");
                throw new IllegalStateException("Public bucket is not configured for PUBLIC attachment usage.");
            }
            return publicBucket;
        }
        return s3Properties.getBucketName();
    }

    private void handleS3Exception(String operation, String key, String bucket, S3Exception e) {
        LOG.error("S3 {} 실패. key={}, bucket={}, errorCode={}, statusCode={}, requestId={}",
                operation,
                key,
                bucket,
                e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "unknown",
                e.statusCode(),
                e.requestId(),
                e);

        if (e.statusCode() == 403 || e.statusCode() == 401) {
            throw new CustomException(AttachmentErrorCode.S3_ACCESS_DENIED, e);
        }
    }
}
