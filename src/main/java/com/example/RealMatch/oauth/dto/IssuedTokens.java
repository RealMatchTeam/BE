package com.example.RealMatch.oauth.dto;

/**
 * 서버 내부에서만 쓰는 토큰 쌍. 리프레시 토큰은 컨트롤러/핸들러가 쿠키로만 내보낸다.
 */
public record IssuedTokens(String accessToken, String refreshToken) {

    public OAuthTokenResponse toResponse() {
        return new OAuthTokenResponse(accessToken);
    }
}
