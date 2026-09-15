package com.example.RealMatch.attachment.application.port;

import java.io.InputStream;

import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;

public interface AttachmentStorage {

    /**
     * 파일을 S3에 업로드한다.
     * usage에 따라 PUBLIC → 퍼블릭 버킷, CHAT → 프라이빗 버킷에 저장.
     */
    void uploadFile(InputStream inputStream, String key, String contentType, long fileSize, AttachmentUsage usage);

    /** CHAT 전용: 프라이빗 버킷 객체에 대한 presigned URL 생성 */
    String generatePresignedUrl(String key);

    String generateStorageKey(AttachmentUsage usage, Long userId, String originalFilename);

    /** 파일 삭제. usage에 따라 대상 버킷 결정. */
    void deleteFile(String key, AttachmentUsage usage);

    /** Return a configured public object key; null means an external URL. */
    String publicStorageKey(String value);

    /** PUBLIC 전용: CloudFront base URL + storageKey로 공개 URL 생성 */
    String buildPublicUrl(String storageKey);

    default boolean isAvailable() {
        return true;
    }
}
