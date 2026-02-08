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
 * <p>2단계 상태머신:
 * <ul>
 *   <li>IN_PROGRESS (짧은 TTL): 선점 직후 상태. 프로세스 다운 시 빠르게 만료 → 재처리 가능.</li>
 *   <li>PROCESSED (긴 TTL): 전송 성공 후 상태. 동일 이벤트의 중복 처리를 장기간 방지.</li>
 * </ul>
 *
 * <p>markIfNotProcessed: 장애/NULL → throw (false 금지). false는 "중복" 의미로만 사용.
 */
@Component
@RequiredArgsConstructor
public class RedisProcessedEventStore implements ProcessedEventStore {

    private static final Logger LOG = LoggerFactory.getLogger(RedisProcessedEventStore.class);
    private static final String KEY_PREFIX = "chat:processed:";

    /** 선점(처리 시작) 상태 값. 짧은 TTL과 함께 사용되어 프로세스 다운 시 빠르게 만료됩니다. */
    private static final String VALUE_IN_PROGRESS = "IN_PROGRESS";
    /** 처리 완료 상태 값. 긴 TTL과 함께 사용되어 중복 처리를 장기간 방지합니다. */
    private static final String VALUE_PROCESSED = "PROCESSED";

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean markIfNotProcessed(String eventId, Duration inProgressTtl) {
        if (eventId == null) {
            throw new IllegalArgumentException("eventId cannot be null");
        }
        if (inProgressTtl == null || inProgressTtl.isNegative() || inProgressTtl.isZero()) {
            throw new IllegalArgumentException("inProgressTtl must be positive");
        }

        String key = KEY_PREFIX + eventId;
        long ttlSeconds = inProgressTtl.getSeconds();

        try {
            // SET key IN_PROGRESS NX EX ttlSeconds
            // NX: 키가 존재하지 않을 때만 설정 (선점)
            // EX: 짧은 TTL → 프로세스 다운 시 빠르게 만료되어 재처리 가능
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(key, VALUE_IN_PROGRESS, inProgressTtl);

            if (result == null) {
                // Redis 응답 null = 판단 불가 → 반드시 실패로 올림 (DLQ/재시도)
                String msg = String.format(
                        "Redis returned null for SETNX. eventId=%s, key=%s", eventId, key);
                LOG.error("[Idempotency] {}", msg);
                throw new IdempotencyStoreException(msg);
            }

            if (result) {
                LOG.debug("[Idempotency] Event marked as IN_PROGRESS. eventId={}, ttl={}s",
                        eventId, ttlSeconds);
                return true; // 선점 성공 (처리 가능)
            } else {
                LOG.debug("[Idempotency] Duplicate event detected. eventId={}", eventId);
                return false; // 중복만 의미 (IN_PROGRESS 또는 PROCESSED 상태)
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
    public void markAsProcessed(String eventId, Duration processedTtl) {
        if (eventId == null) {
            throw new IllegalArgumentException("eventId cannot be null");
        }
        if (processedTtl == null || processedTtl.isNegative() || processedTtl.isZero()) {
            throw new IllegalArgumentException("processedTtl must be positive");
        }

        String key = KEY_PREFIX + eventId;
        long ttlSeconds = processedTtl.getSeconds();

        try {
            // IN_PROGRESS → PROCESSED로 승격. 긴 TTL로 중복 처리를 장기간 방지.
            redisTemplate.opsForValue().set(key, VALUE_PROCESSED, processedTtl);
            LOG.debug("[Idempotency] Event promoted to PROCESSED. eventId={}, ttl={}s",
                    eventId, ttlSeconds);
        } catch (Exception ex) {
            String msg = String.format(
                    "Failed to mark event as PROCESSED. eventId=%s, key=%s, ttl=%ds",
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
            LOG.warn("[Idempotency] Failed to remove processed event key. eventId={}, key={}",
                    eventId, key, ex);
            // 삭제 실패해도 TTL로 자동 만료되므로 치명적이지 않음
        }
    }
}
