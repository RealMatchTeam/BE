package com.example.RealMatch.chat.application.cache;

import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatCacheStore {

    private static final Logger LOG = LoggerFactory.getLogger(ChatCacheStore.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value == null ? Optional.empty() : Optional.of(objectMapper.convertValue(value, type));
        } catch (RuntimeException ex) {
            LOG.warn("Cache read failed; using DB. key={}", key, ex);
            return Optional.empty();
        }
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (RuntimeException ex) {
            LOG.warn("Cache write failed. key={}", key, ex);
        }
    }

    public long getVersion(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value == null ? 0L : Long.parseLong(value.toString());
        } catch (RuntimeException ex) {
            LOG.warn("Cache version unavailable; bypassing cache. key={}", key, ex);
            return -1L;
        }
    }

    public long bumpVersion(String key) {
        try {
            Long version = redisTemplate.opsForValue().increment(key);
            return version != null ? version : -1L;
        } catch (RuntimeException ex) {
            LOG.warn("Cache invalidation failed; existing entries expire by TTL. key={}", key, ex);
            return -1L;
        }
    }
}
