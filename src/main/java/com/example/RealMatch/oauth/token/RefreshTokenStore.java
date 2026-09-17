package com.example.RealMatch.oauth.token;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 발급된 리프레시 토큰의 jti 를 Redis 에 보관한다.
 * 토큰 자체가 아니라 무작위 jti 만 저장하므로 Redis 가 유출돼도 토큰이 복원되지 않는다.
 * 재발급 시 기존 jti 를 지우고 새 jti 를 저장(로테이션)하므로 한 번 쓴 리프레시 토큰은 재사용할 수 없다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;

    public void save(Long userId, String jti, Duration ttl) {
        redisTemplate.opsForValue().set(key(userId, jti), "1", ttl);
    }

    /**
     * 저장된 jti 를 삭제하면서 존재 여부를 반환한다.
     * DEL 은 원자적이므로 같은 토큰으로 동시에 재발급을 시도해도 한 요청만 성공한다.
     */
    public boolean consume(Long userId, String jti) {
        return Boolean.TRUE.equals(redisTemplate.delete(key(userId, jti)));
    }

    private String key(Long userId, String jti) {
        return KEY_PREFIX + userId + ":" + jti;
    }
}
