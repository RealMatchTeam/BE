package com.example.RealMatch.global.config.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.config.oauth.CustomOAuth2User;
import com.example.RealMatch.global.config.oauth.KakaoUserInfo;
import com.example.RealMatch.global.config.oauth.NaverUserInfo;
import com.example.RealMatch.global.config.oauth.OAuth2UserInfo;
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
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 제공자별로 정보 추출
        OAuth2UserInfo userInfo = getOAuth2UserInfo(provider, attributes);

        // DB 저장 및 업데이트
        User user = userRepository.findByProviderIdAndProvider(
                        userInfo.getProviderId(),
                        userInfo.getProvider()
                )
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .providerId(userInfo.getProviderId())
                            .provider(userInfo.getProvider())
                            .email(userInfo.getEmail())
                            .name(userInfo.getName())
                            .role("USER")
                            .build();
                    return userRepository.save(newUser);
                });

        // 이메일이나 이름이 변경된 경우 업데이트
        if (shouldUpdateProfile(user, userInfo)) {
            user.updateProfile(userInfo.getEmail(), userInfo.getName());
            userRepository.save(user);
        }

        return new CustomOAuth2User(user, attributes);
    }

    private OAuth2UserInfo getOAuth2UserInfo(String provider, Map<String, Object> attributes) {
        switch (provider) {
            case "kakao":
                return new KakaoUserInfo(attributes);
            case "naver":
                return new NaverUserInfo(attributes);
            default:
                throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + provider);
        }
    }

    private boolean shouldUpdateProfile(User user, OAuth2UserInfo userInfo) {
        String newEmail = userInfo.getEmail();
        String newName = userInfo.getName();

        return newEmail != null && !newEmail.equals(user.getEmail()) ||
                newName != null && !newName.equals(user.getName());
    }
}
