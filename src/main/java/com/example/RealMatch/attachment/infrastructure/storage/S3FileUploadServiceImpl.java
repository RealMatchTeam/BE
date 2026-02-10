package com.example.RealMatch.attachment.infrastructure.storage;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

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
public class S3FileUploadServiceImpl implements S3FileUploadService {

    private static final Logger LOG = LoggerFactory.getLogger(S3FileUploadServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;
    private final S3FileNameSanitizer fileNameSanitizer;

    @Override
    public String uploadFile(InputStream inputStream, String key, String contentType, long fileSize, AttachmentUsage usage) {
        String bucket = resolveBucket(usage);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(fileSize)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, fileSize));
            LOG.info("S3 업로드 완료. bucket={}, key={}, usage={}", bucket, key, usage);
            return null;

        } catch (S3Exception e) {
            handleS3Exception("파일 업로드", key, bucket, e);
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED);
        } catch (Exception e) {
            LOG.error("S3 파일 업로드 중 예상치 못한 오류 발생. bucket={}, key={}", bucket, key, e);
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED);
        }
    }

    @Override
    public String generatePresignedUrl(String key, int expirationSeconds) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                    presigner -> presigner
                            .signatureDuration(java.time.Duration.ofSeconds(expirationSeconds))
                            .getObjectRequest(getObjectRequest)
            );

            return presignedRequest.url().toString();

        } catch (S3Exception e) {
            handleS3Exception("Presigned URL 생성", key, s3Properties.getBucketName(), e);
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED);
        } catch (Exception e) {
            LOG.error("Presigned URL 생성 중 예상치 못한 오류 발생. key={}", key, e);
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED);
        }
    }

    @Override
    public String generateS3Key(AttachmentUsage usage, Long userId, Long attachmentId, String originalFilename) {
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
            throw new CustomException(AttachmentErrorCode.S3_DELETE_FAILED);
        } catch (Exception e) {
            LOG.error("S3 파일 삭제 중 예상치 못한 오류 발생. bucket={}, key={}", bucket, key, e);
            throw new CustomException(AttachmentErrorCode.S3_DELETE_FAILED);
        }
    }

    @Override
    public String buildPublicUrl(String storageKey) {
        String base = s3Properties.getCloudfrontBaseUrl();
        if (base == null || base.isBlank()) {
            LOG.warn("CloudFront base URL이 설정되지 않았습니다. storageKey={}", storageKey);
            return null;
        }
        return base.endsWith("/") ? base + storageKey : base + "/" + storageKey;
    }

    /**
     * usage에 따라 대상 버킷 결정.
     * PUBLIC → publicBucketName, CHAT → bucketName (private).
     */
    private String resolveBucket(AttachmentUsage usage) {
        if (usage == AttachmentUsage.PUBLIC) {
            String publicBucket = s3Properties.getPublicBucketName();
            if (publicBucket == null || publicBucket.isBlank()) {
                LOG.warn("PUBLIC 버킷이 미설정. private 버킷으로 fallback합니다.");
                return s3Properties.getBucketName();
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
                e.awsErrorDetails().errorCode(),
                e.statusCode(),
                e.requestId(),
                e);
        
        if (e.statusCode() == 403 || e.statusCode() == 401) {
            throw new CustomException(AttachmentErrorCode.S3_ACCESS_DENIED);
        }
    }
}
