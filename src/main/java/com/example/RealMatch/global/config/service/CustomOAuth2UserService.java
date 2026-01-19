package com.example.RealMatch.global.config.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.config.oauth.CustomOAuth2User;
import com.example.RealMatch.match.domain.entity.User;
import com.example.RealMatch.match.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        //xs 기본 loadUser 호출
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 정보 추출
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "kakao"
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오는 id가 Long으로 오기 때문에 String.valueOf로 안전하게 변환
        String providerId = String.valueOf(attributes.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (kakaoAccount != null) ? (String) kakaoAccount.get("email") : null;

        String name = null;
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                name = (String) profile.get("nickname");
            }
        }

        // DB 저장 및 업데이트
        String finalName = name;
        User user = userRepository.findByProviderIdAndProvider(providerId, provider)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .providerId(providerId)
                            .provider(provider)
                            .email(email)
                            .name(finalName)
                            .role("USER")
                            .build();
                    return userRepository.save(newUser);
                });

        if (email != null && !email.equals(user.getEmail())) {
            user.updateProfile(email, name);
            userRepository.save(user);
        }

        // CustomOAuth2User 반환
        return new CustomOAuth2User(user, attributes);
    }
}
