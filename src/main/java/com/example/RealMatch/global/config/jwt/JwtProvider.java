package com.example.RealMatch.global.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpireMillis;
    private final long refreshTokenExpireMillis;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expire-ms}") long accessTokenExpireMillis,
            @Value("${jwt.refresh-expire-ms}") long refreshTokenExpireMillis
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
//        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpireMillis = accessTokenExpireMillis;
        this.refreshTokenExpireMillis = refreshTokenExpireMillis;
    }

    // =========================
    //   토큰 생성
    // =========================
    public String createAccessToken(Long userId, String providerId, String role) {
        return createToken(userId, providerId, role, accessTokenExpireMillis);
    }

    public String createRefreshToken(Long userId, String providerId, String role) {
        return createToken(userId, providerId, role, refreshTokenExpireMillis);
    }

    private String createToken(Long userId, String providerId, String role, long expireMillis) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("providerId", providerId)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireMillis))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    //   토큰 검증
    // =========================
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;  // 만료
        } catch (JwtException | IllegalArgumentException e) {
            return false;  // 변조, 손상 등
        }
    }

    // =========================
    //   Claims 가져오기
    // =========================
    public Claims getClaims(String token) {
        return parseClaims(token).getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public String getProviderId(String token) {
        return getClaims(token).get("providerId", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)  // Key 타입 강제
                .build()
                .parseSignedClaims(token);
    }
}

