package com.example.RealMatch.oauth.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.oauth.code.OAuthErrorCode;
import com.example.RealMatch.oauth.dto.CustomOAuth2User;
import com.example.RealMatch.oauth.dto.GoogleUserInfo;
import com.example.RealMatch.oauth.dto.KakaoUserInfo;
import com.example.RealMatch.oauth.dto.NaverUserInfo;
import com.example.RealMatch.oauth.dto.OAuth2UserInfo;
import com.example.RealMatch.user.domain.entity.AuthenticationMethod;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.AuthenticationMethodRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuthenticationMethodRepository authenticationMethodRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration()
                .getRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo userInfo = getOAuth2UserInfo(provider, attributes);

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new CustomException(OAuthErrorCode.EMAIL_NOT_PROVIDED);
        }

        // 이미 가입된 유저인지 확인
        AuthenticationMethod authMethod =
                authenticationMethodRepository
                        .findByProviderAndProviderId(
                                userInfo.getProvider(),
                                userInfo.getProviderId()
                        )
                        .orElseGet(() -> registerNewUser(userInfo));

        return new CustomOAuth2User(
                authMethod.getUser().getId(),
                authMethod.getUser().getRole().name(),
                authMethod.getProvider(),
                authMethod.getEmail(),
                attributes
        );
    }

    private AuthenticationMethod registerNewUser(OAuth2UserInfo userInfo) {
        // User 생성
        User user = userRepository.save(
                User.builder()
                        .name(userInfo.getName())
                        .nickname(userInfo.getName()) // 초기에 닉네임은 이름과 동일하게 설정
                        .email(userInfo.getEmail())
                        .role(Role.GUEST) // [수정] 최초 가입 시 GUEST 권한 부여
                        .build()
        );

        // AuthenticationMethod 생성
        return authenticationMethodRepository.save(
                AuthenticationMethod.builder()
                        .user(user)
                        .provider(userInfo.getProvider())
                        .providerId(userInfo.getProviderId())
                        .email(userInfo.getEmail())
                        .build()
        );
    }

    private OAuth2UserInfo getOAuth2UserInfo(
            String provider,
            Map<String, Object> attributes
    ) {
        switch (provider) {
            case "kakao":
                return new KakaoUserInfo(attributes);
            case "naver":
                return new NaverUserInfo(attributes);
            case "google":
                return new GoogleUserInfo(attributes);
            default:
                throw new CustomException(OAuthErrorCode.UNSUPPORTED_PROVIDER);
        }
    }
}
