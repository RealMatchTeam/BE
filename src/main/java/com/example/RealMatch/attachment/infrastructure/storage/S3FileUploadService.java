package com.example.RealMatch.attachment.infrastructure.storage;

import java.io.InputStream;

import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;

public interface S3FileUploadService {

    /**
     * 파일을 S3에 업로드한다.
     * usage에 따라 PUBLIC → 퍼블릭 버킷, CHAT → 프라이빗 버킷에 저장.
     */
    String uploadFile(InputStream inputStream, String key, String contentType, long fileSize, AttachmentUsage usage);

    /** CHAT 전용: 프라이빗 버킷 객체에 대한 presigned URL 생성 */
    String generatePresignedUrl(String key, int expirationSeconds);

    String generateS3Key(AttachmentUsage usage, Long userId, Long attachmentId, String originalFilename);

    /** 파일 삭제. usage에 따라 대상 버킷 결정. */
    void deleteFile(String key, AttachmentUsage usage);

    /** PUBLIC 전용: CloudFront base URL + storageKey로 공개 URL 생성 */
    String buildPublicUrl(String storageKey);

    default boolean isAvailable() {
        return true;
    }
}
