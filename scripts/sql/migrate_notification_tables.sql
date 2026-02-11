-- ============================================
-- 알림 관련 테이블 마이그레이션 (전체)
-- ============================================
-- 실행 순서: notification → notification_delivery → notification_outbox, fcm_token
-- MySQL 8.0+ / MariaDB 10.2+ 권장 (DATETIME(6), utf8mb4)
-- 사용법: mysql -u 사용자 -p DB명 < migrate_notification_tables.sql
-- ============================================

-- ---------------------------------------------------------------------------
-- 1. Notification (알림 원장)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '알림 수신자 사용자 ID',
    kind VARCHAR(30) NOT NULL COMMENT '알림 종류 (PROPOSAL_RECEIVED, CAMPAIGN_MATCHED 등)',
    title VARCHAR(255) NOT NULL COMMENT '알림 제목',
    body VARCHAR(1000) NOT NULL COMMENT '알림 내용',
    reference_type VARCHAR(30) COMMENT '참조 타입 (CAMPAIGN_PROPOSAL, CAMPAIGN_APPLY, CAMPAIGN)',
    reference_id VARCHAR(36) COMMENT '참조 ID',
    campaign_id BIGINT COMMENT '관련 캠페인 ID',
    proposal_id BIGINT COMMENT '관련 제안 ID',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '읽음 여부',
    created_at DATETIME(6) NOT NULL COMMENT '생성 시간',
    updated_at DATETIME(6) COMMENT '수정 시간',
    deleted_at DATETIME(6) COMMENT '삭제 시간 (소프트 삭제)',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부 (소프트 삭제)',
    INDEX idx_notification_user_read_created (user_id, is_read, created_at) COMMENT '미읽음 조회 최적화',
    INDEX idx_notification_user_created (user_id, created_at) COMMENT '날짜별 조회 최적화',
    INDEX idx_notification_user_kind (user_id, kind) COMMENT '필터 조회 최적화'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='알림 원장 테이블';

-- ---------------------------------------------------------------------------
-- 2. NotificationDelivery (채널별 발송 추적, 재시도)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification_delivery (
    id BINARY(16) NOT NULL PRIMARY KEY,
    notification_id BINARY(16) NOT NULL,
    channel VARCHAR(30) NOT NULL COMMENT 'PUSH, EMAIL, SMS',
    status VARCHAR(20) NOT NULL COMMENT 'PENDING, IN_PROGRESS, RETRY, SENT, FAILED',
    fail_reason VARCHAR(500),
    attempted_at DATETIME(6),
    next_retry_at DATETIME(6) COMMENT '다음 재시도 예정 시간 (exponential backoff: 1m, 5m, 30m, 2h, 12h)',
    sent_at DATETIME(6),
    provider_message_id VARCHAR(255),
    idempotency_key VARCHAR(100) UNIQUE COMMENT 'eventId:kind:receiverId:channel 조합, 멱등성 보장',
    attempt_count INT NOT NULL DEFAULT 0 COMMENT '발송 시도 횟수 (최대 5회, 재시도 정책용)',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    INDEX idx_delivery_notification (notification_id, channel),
    INDEX idx_delivery_status_retry (status, next_retry_at, created_at) COMMENT '워커 배치 조회 최적화'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='푸시/이메일 발송 추적 (Outbox + RabbitMQ 발송용)';

-- ---------------------------------------------------------------------------
-- 3. NotificationOutbox (Outbox 패턴, MQ 발행 대기)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification_outbox (
    id BINARY(16) NOT NULL PRIMARY KEY,
    delivery_id BINARY(16) NOT NULL COMMENT 'FK → notification_delivery.id',
    notification_id BINARY(16) NOT NULL COMMENT 'FK → notification.id',
    channel VARCHAR(30) NOT NULL COMMENT 'PUSH, EMAIL',
    status VARCHAR(20) NOT NULL COMMENT 'PENDING, SENDING, SENT, FAILED',
    retry_count INT NOT NULL DEFAULT 0 COMMENT 'MQ 발행 재시도 횟수 (최대 10회)',
    last_error VARCHAR(500) COMMENT '마지막 발행 실패 사유',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    INDEX idx_outbox_status_created (status, created_at) COMMENT 'PENDING Outbox 조회 (OutboxPublisher)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Outbox: Notification+Delivery와 동일 TX 저장, Publisher가 MQ 발행';

-- ---------------------------------------------------------------------------
-- 4. FCM Token (웹 푸시 디바이스 토큰)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS fcm_token (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '토큰 소유자 사용자 ID',
    token VARCHAR(500) NOT NULL COMMENT 'FCM 디바이스 토큰 (웹 푸시용)',
    device_info VARCHAR(255) COMMENT '디바이스 정보 (예: Chrome/120.0.0.0 Windows 10)',
    created_at DATETIME(6) NOT NULL COMMENT '토큰 등록 시간',
    updated_at DATETIME(6) COMMENT '토큰 업데이트 시간',
    INDEX idx_fcm_token_user (user_id),
    UNIQUE KEY uk_fcm_token_value (token) COMMENT '토큰 중복 방지 (멱등성 보장)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='FCM 디바이스 토큰 저장소 (사용자당 여러 토큰 지원)';

-- ============================================
-- 완료
-- ============================================
