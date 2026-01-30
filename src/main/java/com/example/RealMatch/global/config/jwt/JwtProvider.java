package com.example.RealMatch.global.config.jwt;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpireMillis;
    private final long refreshTokenExpireMillis;
    private final String masterJwt; // 마스터 JWT 필드

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expire-ms}") long accessTokenExpireMillis,
            @Value("${jwt.refresh-expire-ms}") long refreshTokenExpireMillis,
            @Value("${jwt.master-jwt:}") String masterJwt
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpireMillis = accessTokenExpireMillis;
        this.refreshTokenExpireMillis = refreshTokenExpireMillis;
        this.masterJwt = masterJwt; // 마스터 JWT 초기화
    }
    // =========================
    //   마스터 JWT 검증
    // =========================
    public boolean isMasterJwt(String token) {
        return masterJwt != null && !masterJwt.isEmpty() && masterJwt.equals(token);
    }

    // =========================
    //   토큰 생성
    // =========================
    public String createAccessToken(Long userId, String providerId, String role, String email) {
        return createToken(userId, providerId, role, email,"access", accessTokenExpireMillis);
    }

    public String createRefreshToken(Long userId, String providerId, String role, String email) {
        return createToken(userId, providerId, role, email,"refresh", refreshTokenExpireMillis);
    }

    private String createToken(Long userId, String providerId, String role, String email, String type, long expireMillis) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("providerId", providerId)
                .claim("role", role)
                .claim("email", email)
                .claim("type", type)
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

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public String getType(String token) {
        return getClaims(token).get("type", String.class);
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)  // Key 타입 강제
                .build()
                .parseSignedClaims(token);
    }
}

