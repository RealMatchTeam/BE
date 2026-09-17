package com.example.RealMatch.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.RealMatch.global.config.jwt.JwtProvider;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.oauth.dto.IssuedTokens;
import com.example.RealMatch.oauth.service.AuthService;
import com.example.RealMatch.oauth.token.RefreshTokenCookieManager;
import com.example.RealMatch.oauth.token.RefreshTokenStore;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.UserRepository;

import jakarta.servlet.http.Cookie;

class RefreshTokenRotationTest {

    private final JwtProvider jwt = new JwtProvider(Base64.getEncoder().encodeToString(new byte[32]), 60_000, 120_000, "");
    private final UserRepository users = mock(UserRepository.class);
    private final Set<String> redisKeys = new HashSet<>();
    private final RefreshTokenStore store = new RefreshTokenStore(inMemoryRedis());
    private final AuthService authService = new AuthService(
            users, null, null, null, null, null, null, jwt, null, null, store);
    private final RefreshTokenCookieManager cookies = new RefreshTokenCookieManager(jwt, true, "Lax", "");

    @Test
    void refreshRotatesTokenAndRejectsReuse() {
        User user = User.builder().role(Role.CREATOR).email("a@example.com").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(users.findById(1L)).thenReturn(Optional.of(user));

        IssuedTokens first = authService.issueTokens(1L, "KAKAO", "CREATOR", "a@example.com");
        IssuedTokens second = authService.refresh(first.refreshToken());

        assertNotEquals(first.refreshToken(), second.refreshToken());
        assertEquals("access", jwt.getType(second.accessToken()));
        // 이미 소비된 리프레시 토큰은 재사용 불가
        assertThrows(CustomException.class, () -> authService.refresh(first.refreshToken()));
        // 로테이션된 최신 토큰은 사용 가능
        authService.refresh(second.refreshToken());
    }

    @Test
    void accessTokenCannotBeUsedAsRefreshToken() {
        IssuedTokens tokens = authService.issueTokens(1L, "KAKAO", "CREATOR", "a@example.com");
        assertThrows(CustomException.class, () -> authService.refresh(tokens.accessToken()));
    }

    @Test
    void revokeInvalidatesStoredTokenWithoutThrowing() {
        IssuedTokens tokens = authService.issueTokens(1L, "KAKAO", "CREATOR", "a@example.com");
        authService.revoke(tokens.refreshToken());
        assertTrue(redisKeys.isEmpty());
        assertThrows(CustomException.class, () -> authService.refresh(tokens.refreshToken()));
        authService.revoke("not-a-jwt"); // 로그아웃은 잘못된 토큰이어도 실패하지 않는다
    }

    @Test
    void cookieIsHttpOnlyScopedToAuthPathAndNeverExposesTokenElsewhere() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        cookies.attach(response, "rt-value");
        String setCookie = response.getHeader("Set-Cookie");

        assertTrue(setCookie.startsWith(RefreshTokenCookieManager.COOKIE_NAME + "=rt-value"));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("Secure"));
        assertTrue(setCookie.contains("SameSite=Lax"));
        assertTrue(setCookie.contains("Path=/api/v1/auth"));
        assertTrue(setCookie.contains("Max-Age=120"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(RefreshTokenCookieManager.COOKIE_NAME, "rt-value"));
        assertEquals(Optional.of("rt-value"), cookies.read(request));
        assertEquals(Optional.empty(), cookies.read(new MockHttpServletRequest()));

        MockHttpServletResponse expired = new MockHttpServletResponse();
        cookies.expire(expired);
        assertTrue(expired.getHeader("Set-Cookie").contains("Max-Age=0"));
        assertFalse(expired.getHeader("Set-Cookie").contains("rt-value"));
    }

    @SuppressWarnings("unchecked")
    private StringRedisTemplate inMemoryRedis() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(ops);
        org.mockito.Mockito.doAnswer(inv -> {
            redisKeys.add(inv.getArgument(0));
            return null;
        }).when(ops).set(anyString(), anyString(), any(java.time.Duration.class));
        when(template.delete(anyString())).thenAnswer(inv -> redisKeys.remove(inv.<String>getArgument(0)));
        return template;
    }
}
