package com.example.RealMatch.chat.application.idempotency;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Redis 기반 이벤트 중복 처리 방지 저장소 구현체.
 */
@Component
@RequiredArgsConstructor
public class RedisProcessedEventStore implements ProcessedEventStore {

    private static final Logger LOG = LoggerFactory.getLogger(RedisProcessedEventStore.class);
    private static final String KEY_PREFIX = "chat:processed:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean markIfNotProcessed(String eventId, Duration ttl) {
        if (eventId == null) {
            throw new IllegalArgumentException("eventId cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("ttl must be positive");
        }

        String key = KEY_PREFIX + eventId;
        long ttlSeconds = ttl.getSeconds();

        try {
            // SET key value NX EX ttlSeconds
            // NX: 키가 존재하지 않을 때만 설정
            // EX: TTL 설정 (초 단위)
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);

            if (result == null) {
                // Redis 응답이 null인 경우 (장애 가능성)
                LOG.error("[Idempotency] Redis returned null for SETNX. eventId={}, key={}", eventId, key);
                // 보수적으로 실패 처리 (중복 카드 방지)
                return false;
            }

            if (result) {
                LOG.debug("[Idempotency] Event marked as processing. eventId={}, ttl={}s", eventId, ttlSeconds);
                return true; // 처리 가능 (처리되지 않음)
            } else {
                LOG.debug("[Idempotency] Duplicate event detected. eventId={}", eventId);
                return false; // 중복 (이미 처리됨)
            }

        } catch (Exception ex) {
            LOG.error("[Idempotency] Redis operation failed. eventId={}, key={}, ttl={}s",
                    eventId, key, ttlSeconds, ex);
            // 보수적으로 실패 처리 (중복 카드가 뜨는 것보다 안전)
            // TODO: 모니터링 시스템에 알림 전송
            return false;
        }
    }

    @Override
    public boolean markAsProcessed(String eventId, Duration ttl) {
        if (eventId == null) {
            throw new IllegalArgumentException("eventId cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("ttl must be positive");
        }

        String key = KEY_PREFIX + eventId;
        long ttlSeconds = ttl.getSeconds();

        try {
            // 전송 성공 후 키를 확실히 남기기 위해 SET EX 사용
            redisTemplate.opsForValue().set(key, "1", ttl);
            LOG.debug("[Idempotency] Event marked as processed (success). eventId={}, ttl={}s", eventId, ttlSeconds);
            return true;
        } catch (Exception ex) {
            LOG.error("[Idempotency] Failed to mark event as processed. eventId={}, key={}, ttl={}s",
                    eventId, key, ttlSeconds, ex);
            return false;
        }
    }

    @Override
    public void removeProcessed(String eventId) {
        if (eventId == null) {
            return;
        }

        String key = KEY_PREFIX + eventId;

        try {
            redisTemplate.delete(key);
            LOG.debug("[Idempotency] Removed processed event key (logical failure). eventId={}", eventId);
        } catch (Exception ex) {
            LOG.warn("[Idempotency] Failed to remove processed event key. eventId={}, key={}", eventId, key, ex);
            // 삭제 실패해도 TTL로 자동 만료되므로 치명적이지 않음
        }
    }
}
