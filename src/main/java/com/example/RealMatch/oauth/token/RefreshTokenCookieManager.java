package com.example.RealMatch.oauth.token;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.example.RealMatch.global.config.jwt.JwtProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 리프레시 토큰을 HttpOnly 쿠키로만 주고받는다.
 * - HttpOnly: JS(localStorage 등)에서 접근 불가 → XSS로 탈취 불가
 * - Path=/api/v1/auth: 재발급/로그아웃 요청에만 쿠키가 실림
 * - SameSite=Lax(기본): 타 사이트에서 시작된 POST 에는 쿠키가 실리지 않아 CSRF 방어
 */
@Component
public class RefreshTokenCookieManager {

    public static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/v1/auth";

    private final JwtProvider jwtProvider;
    private final boolean secure;
    private final String sameSite;
    private final String domain;

    public RefreshTokenCookieManager(
            JwtProvider jwtProvider,
            @Value("${auth.refresh-cookie.secure:true}") boolean secure,
            @Value("${auth.refresh-cookie.same-site:Lax}") String sameSite,
            @Value("${auth.refresh-cookie.domain:}") String domain
    ) {
        this.jwtProvider = jwtProvider;
        this.secure = secure;
        this.sameSite = sameSite;
        this.domain = domain;
    }

    public void attach(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = baseCookie(refreshToken)
                .maxAge(Duration.ofMillis(jwtProvider.getRefreshTokenExpireMillis()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void expire(HttpServletResponse response) {
        ResponseCookie cookie = baseCookie("")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public Optional<String> read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(COOKIE_PATH);
        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }
        return builder;
    }
}
