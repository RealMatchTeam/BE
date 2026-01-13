package com.example.RealMatch.global.auth.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.RealMatch.global.auth.details.CustomOAuth2User;
import com.example.RealMatch.global.auth.oauth.OAuthAttributes;
import com.example.RealMatch.global.auth.oauth.OAuthUserInfo;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        log.info("=== OAuth2 Login Start ===");
        log.info("Provider: {}", provider);
        log.debug("OAuth2 attributes: {}", oAuth2User.getAttributes());

        try {
            // Provider별 사용자 정보 파싱
            OAuthUserInfo userInfo = OAuthAttributes.of(provider, oAuth2User.getAttributes());

            log.info("Parsed user info:");
            log.info("  - Provider: {}", provider);
            log.info("  - ProviderId: {}", userInfo.getProviderId());
            log.info("  - Email: {}", userInfo.getEmail());
            log.info("  - Name: {}", userInfo.getName());

            // DB에서 사용자 조회 또는 생성
            User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                    .map(existingUser -> {
                        log.info("Existing user found: {}", existingUser.getId());
                        existingUser.updateProfile(userInfo.getEmail(), userInfo.getName());
                        return userRepository.save(existingUser);
                    })
                    .orElseGet(() -> {
                        log.info("Creating new user");
                        User newUser = User.createOAuthUser(
                                userInfo.getEmail(),
                                userInfo.getName(),
                                provider,
                                userInfo.getProviderId()
                        );
                        return userRepository.save(newUser);
                    });

            log.info("User authenticated successfully - ID: {}, Name: {}",
                    user.getId(), user.getName());
            log.info("=== OAuth2 Login End ===");

            return new CustomOAuth2User(user, oAuth2User.getAttributes());

        } catch (Exception e) {
            log.error("Error during OAuth2 user processing", e);
            throw new OAuth2AuthenticationException("OAuth2 processing failed: " + e.getMessage());
        }
    }
}
