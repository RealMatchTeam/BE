package com.example.RealMatch.attachment.domain.enums;

/**
 * 첨부파일 용도
 * S3 경로 prefix 분리 및 향후 TTL·캐싱 정책 분리의 기준
 *
 * CHAT: 채팅 첨부. 참여자만 접근 가능. 비공개 자산.
 * PUBLIC: 브랜드/캠페인 등 상세페이지·홈 화면 노출용. 로그인 없이도 이미지 조회 가능해야 하는 공개 자산.
 */
public enum AttachmentUsage {
    CHAT,
    PUBLIC
}
