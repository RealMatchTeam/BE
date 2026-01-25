package com.example.RealMatch.global.oauth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.user.domain.entity.ContentCategory;
import com.example.RealMatch.user.domain.entity.SignupPurpose;
import com.example.RealMatch.user.domain.entity.Term;
import com.example.RealMatch.user.domain.entity.UserContentCategory;
import com.example.RealMatch.user.domain.entity.UserSignupPurpose;
import com.example.RealMatch.user.domain.entity.UserTerm;
import com.example.RealMatch.user.domain.repository.SignupPurposeRepository;
import com.example.RealMatch.user.domain.repository.TermRepository;
import com.example.RealMatch.user.domain.repository.UserContentCategoryRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.domain.repository.UserSignupPurposeRepository;
import com.example.RealMatch.user.domain.repository.UserTermRepository;
import com.example.RealMatch.global.config.jwt.JwtProvider;
import com.example.RealMatch.global.oauth.code.OAuthErrorCode;
import com.example.RealMatch.global.oauth.dto.OAuthTokenResponse;
import com.example.RealMatch.global.oauth.dto.request.SignupCompleteRequest;
import com.example.RealMatch.global.oauth.exception.AuthException;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.ContentCategoryRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final TermRepository termRepository;
    private final UserTermRepository userTermRepository;
    private final SignupPurposeRepository signupPurposeRepository;
    private final UserSignupPurposeRepository userSignupPurposeRepository;
    private final ContentCategoryRepository contentCategoryRepository;
    private final UserContentCategoryRepository userContentCategoryRepository;
    private final JwtProvider jwtProvider;

    public OAuthTokenResponse completeSignup(Long userId, SignupCompleteRequest request) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(OAuthErrorCode.USER_NOT_FOUND));

        // GUEST가 아니면 이미 가입 완료된 유저
        if (!user.getRole().equals(Role.GUEST)) {
            throw new AuthException(OAuthErrorCode.ALREADY_SIGNED_UP);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.nickname())) {
            throw new AuthException(OAuthErrorCode.NICKNAME_DUPLICATED);
        }

        // 유저 정보 업데이트
        user.completeSignup(
                request.nickname(),
                request.birth(),
                request.gender(),
                request.role()
        );

        // 약관 동의 저장
        saveTermAgreements(user, request.terms());

        // 가입 목적 저장
        saveSignupPurposes(user, request.signupPurposeIds());

        // 콘텐츠 카테고리 저장
        saveContentCategories(user, request.contentCategoryIds());

        String userEmail = user.getEmail();
        String currentRole = user.getRole().name();

        String accessToken = jwtProvider.createAccessToken(user.getId(), userEmail, currentRole);
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), userEmail, currentRole);

        return new OAuthTokenResponse(accessToken, refreshToken);
    }

    public OAuthTokenResponse refreshAccessToken(String refreshTokenHeader) {
        String refreshToken = extractToken(refreshTokenHeader);

        // 토큰 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new AuthException(OAuthErrorCode.INVALID_TOKEN);
        }

        // 토큰 타입 검증
        if (!"refresh".equals(jwtProvider.getType(refreshToken))) {
            throw new AuthException(OAuthErrorCode.NOT_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        String providerId = jwtProvider.getProviderId(refreshToken);
        String role = jwtProvider.getRole(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(OAuthErrorCode.USER_NOT_FOUND));

        // 3. 토큰 갱신
        String newAccessToken = jwtProvider.createAccessToken(user.getId(), providerId, role);

        return new OAuthTokenResponse(newAccessToken, refreshToken);
    }

    private String extractToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return header;
    }

    private void saveTermAgreements(User user, List<SignupCompleteRequest.TermAgreementDto> terms) {
        if (terms != null) {
            terms.forEach(dto -> {
                Term term = termRepository.findByName(dto.type())
                        .orElseThrow(() -> new AuthException(OAuthErrorCode.TERM_NOT_FOUND));
                userTermRepository.save(UserTerm.builder()
                        .user(user)
                        .term(term)
                        .isAgreed(dto.agreed())
                        .build());
            });
        }
    }

    private void saveSignupPurposes(User user, List<Long> signupPurposeIds) {
        if (signupPurposeIds != null) {
            signupPurposeIds.forEach(id -> {
                SignupPurpose purpose = signupPurposeRepository.findById(id)
                        .orElseThrow(() -> new AuthException(OAuthErrorCode.PURPOSE_NOT_FOUND));

                userSignupPurposeRepository.save(UserSignupPurpose.builder()
                        .user(user)
                        .purpose(purpose)
                        .build());
            });
        }
    }

    private void saveContentCategories(User user, List<Long> contentCategoryIds) {
        if (contentCategoryIds != null) {
            contentCategoryIds.forEach(id -> {
                ContentCategory category = contentCategoryRepository.findById(id)
                        .orElseThrow(() -> new AuthException(OAuthErrorCode.CATEGORY_NOT_FOUND));

                userContentCategoryRepository.save(UserContentCategory.builder()
                        .user(user)
                        .contentCategory(category)
                        .build());
            });
        }
    }
}