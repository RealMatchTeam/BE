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
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.convertValue(cached, type));
        } catch (IllegalArgumentException ex) {
            LOG.warn("Failed to convert cached value. key={}", key, ex);
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public long getVersion(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return 1L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            LOG.warn("Failed to parse version key. key={}, value={}", key, value);
            return 1L;
        }
    }

    public long bumpVersion(String key) {
        Long newVersion = redisTemplate.opsForValue().increment(key);
        return newVersion != null ? newVersion : 1L;
    }
}
