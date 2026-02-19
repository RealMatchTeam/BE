package com.example.RealMatch.notification.infrastructure.redis;

import java.time.Duration;
import java.util.OptionalLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

/**
 * 미읽음 알림 개수 조회 성능 최적화를 위한 Redis 캐시.
 * <p>캐시 키: notification:unread:{userId}
 * <p>캐시 무효화: 알림 생성, 읽음 처리, 전체 읽기, 소프트 삭제 시 호출
 * <p>Redis 장애 시 DB 조회로 폴백
 */
@Component
@RequiredArgsConstructor
public class NotificationUnreadCountCache {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationUnreadCountCache.class);
    private static final String KEY_PREFIX = "notification:unread:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public OptionalLong get(Long userId) {
        if (userId == null) {
            return OptionalLong.empty();
        }
        try {
            String key = KEY_PREFIX + userId;
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            invalidate(userId);
            return OptionalLong.empty();
        } catch (Exception e) {
            LOG.warn("[UnreadCountCache] Redis get failed, fallback to DB. userId={}", userId, e);
            return OptionalLong.empty();
        }
    }

    public void set(Long userId, long count) {
        if (userId == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + userId, String.valueOf(count), TTL);
        } catch (Exception e) {
            LOG.warn("[UnreadCountCache] Redis set failed. userId={}", userId, e);
        }
    }

    public void invalidate(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            redisTemplate.delete(KEY_PREFIX + userId);
        } catch (Exception e) {
            LOG.warn("[UnreadCountCache] Redis invalidate failed. userId={}", userId, e);
        }
    }

    /**
     * 트랜잭션 커밋 완료 후 캐시를 무효화합니다.
     * <p>커밋 이전 무효화 시 다른 스레드가 아직 커밋되지 않은 DB 값을 읽어
     * 캐시에 저장하는 레이스 컨디션을 방지합니다.
     */
    public void invalidateAfterCommit(Long userId) {
        if (userId == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            invalidate(userId);
                        }
                    }
            );
        } else {
            invalidate(userId);
        }
    }
}
