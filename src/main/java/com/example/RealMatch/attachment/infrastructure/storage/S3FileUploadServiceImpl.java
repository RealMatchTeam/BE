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
    public String uploadFile(InputStream inputStream, String key, String contentType, long fileSize) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .contentLength(fileSize)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, fileSize));
            return null;

        } catch (S3Exception e) {
            handleS3Exception("파일 업로드", key, e);
            throw new CustomException(AttachmentErrorCode.S3_UPLOAD_FAILED);
        } catch (Exception e) {
            LOG.error("S3 파일 업로드 중 예상치 못한 오류 발생. key={}", key, e);
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
            handleS3Exception("Presigned URL 생성", key, e);
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
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            handleS3Exception("파일 삭제", key, e);
            throw new CustomException(AttachmentErrorCode.S3_DELETE_FAILED);
        } catch (Exception e) {
            LOG.error("S3 파일 삭제 중 예상치 못한 오류 발생. key={}", key, e);
            throw new CustomException(AttachmentErrorCode.S3_DELETE_FAILED);
        }
    }

    private void handleS3Exception(String operation, String key, S3Exception e) {
        LOG.error("S3 {} 실패. key={}, errorCode={}, statusCode={}, requestId={}, bucket={}",
                operation,
                key,
                e.awsErrorDetails().errorCode(),
                e.statusCode(),
                e.requestId(),
                s3Properties.getBucketName(),
                e);
        
        if (e.statusCode() == 403 || e.statusCode() == 401) {
            throw new CustomException(AttachmentErrorCode.S3_ACCESS_DENIED);
        }
    }
}
