package com.example.RealMatch.global.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    // OAuth2 로그인 성공 후 redirect URL
    @GetMapping("/login/success")
    public String loginSuccess(
            @RequestParam String accessToken,
            @RequestParam String refreshToken
    ) {
        // 간단하게 HTML로 화면에 토큰 출력
        return "<html>" +
                "<body>" +
                "<h1>로그인 성공!</h1>" +
                "<p>Access Token: " + accessToken + "</p>" +
                "<p>Refresh Token: " + refreshToken + "</p>" +
                "</body>" +
                "</html>";
    }
}
