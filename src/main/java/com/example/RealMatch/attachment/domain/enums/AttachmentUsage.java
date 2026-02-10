package com.example.RealMatch.attachment.domain.enums;

/**
 * 첨부파일의 용도를 정의합니다.
 * <p>
 * 이 값에 따라 파일이 저장되는 S3 버킷(private/public)과 접근 URL 생성 방식(presigned/CDN)이 결정됩니다.
 * <ul>
 *     <li>{@code CHAT}: 채팅 등 비공개 컨텍스트에서 사용되는 파일. private 버킷에 저장되며, presigned URL로 접근.</li>
 *     <li>{@code PUBLIC}: 브랜드 로고, 캠페인 이미지 등 공개 자산. public 버킷에 저장되며, CloudFront(CDN) URL로 접근.</li>
 * </ul>
 */
public enum AttachmentUsage {
    CHAT,
    PUBLIC
}
