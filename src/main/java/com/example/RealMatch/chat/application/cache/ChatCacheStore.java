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
}
