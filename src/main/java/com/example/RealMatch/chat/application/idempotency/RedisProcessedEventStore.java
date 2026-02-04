package com.example.RealMatch.chat.application.idempotency;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.exception.IdempotencyStoreException;

import lombok.RequiredArgsConstructor;

/**
 * Redis 기반 이벤트 중복 처리 방지 저장소 구현체.
 *
 * <p>markIfNotProcessed: 장애/NULL → throw (false 금지). false는 "중복" 의미로만 사용.
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
                // Redis 응답 null = 판단 불가 → 반드시 실패로 올림 (DLQ/재시도)
                String msg = String.format("Redis returned null for SETNX. eventId=%s, key=%s", eventId, key);
                LOG.error("[Idempotency] {}", msg);
                throw new IdempotencyStoreException(msg);
            }

            if (result) {
                LOG.debug("[Idempotency] Event marked as processing. eventId={}, ttl={}s", eventId, ttlSeconds);
                return true; // 선점 성공 (처리 가능)
            } else {
                LOG.debug("[Idempotency] Duplicate event detected. eventId={}", eventId);
                return false; // 중복만 의미
            }

        } catch (IdempotencyStoreException e) {
            throw e;
        } catch (Exception ex) {
            // Redis 장애/타임아웃 등 판단 불가 → 반드시 실패로 올림
            String msg = String.format("Redis operation failed. eventId=%s, key=%s, ttl=%ds",
                    eventId, key, ttlSeconds);
            LOG.error("[Idempotency] {}", msg, ex);
            throw new IdempotencyStoreException(msg, ex);
        }
    }

    @Override
    public void markAsProcessed(String eventId, Duration ttl) {
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
        } catch (Exception ex) {
            String msg = String.format("Failed to mark event as processed. eventId=%s, key=%s, ttl=%ds",
                    eventId, key, ttlSeconds);
            LOG.error("[Idempotency] {}", msg, ex);
            throw new IdempotencyStoreException(msg, ex);
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
            LOG.debug("[Idempotency] Removed processed event key. eventId={}", eventId);
        } catch (Exception ex) {
            LOG.warn("[Idempotency] Failed to remove processed event key. eventId={}, key={}", eventId, key, ex);
            // 삭제 실패해도 TTL로 자동 만료되므로 치명적이지 않음
        }
    }
}
