package com.example.RealMatch.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 클라이언트에는 액세스 토큰만 내려준다. (메모리에만 보관할 것)
 * 리프레시 토큰은 HttpOnly 쿠키로만 전달되며 응답 본문에 포함되지 않는다.
 */
@Getter
@Builder
@AllArgsConstructor
public class OAuthTokenResponse {

    private String accessToken;
}
