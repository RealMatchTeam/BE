package com.example.RealMatch.oauth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.config.jwt.JwtProvider;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.oauth.code.OAuthErrorCode;
import com.example.RealMatch.oauth.dto.OAuthTokenResponse;
import com.example.RealMatch.oauth.dto.request.SignupCompleteRequest;
import com.example.RealMatch.user.domain.entity.ContentCategory;
import com.example.RealMatch.user.domain.entity.SignupPurpose;
import com.example.RealMatch.user.domain.entity.Term;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.UserContentCategory;
import com.example.RealMatch.user.domain.entity.UserSignupPurpose;
import com.example.RealMatch.user.domain.entity.UserTerm;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.ContentCategoryRepository;
import com.example.RealMatch.user.domain.repository.SignupPurposeRepository;
import com.example.RealMatch.user.domain.repository.TermRepository;
import com.example.RealMatch.user.domain.repository.UserContentCategoryRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.domain.repository.UserSignupPurposeRepository;
import com.example.RealMatch.user.domain.repository.UserTermRepository;

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

    public OAuthTokenResponse completeSignup(Long userId, String providerId, SignupCompleteRequest request) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(OAuthErrorCode.USER_NOT_FOUND));

        // GUEST가 아니면 이미 가입 완료된 유저
        if (!user.getRole().equals(Role.GUEST)) {
            throw new CustomException(OAuthErrorCode.ALREADY_SIGNED_UP);
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

        String currentRole = user.getRole().name();

        String accessToken = jwtProvider.createAccessToken(user.getId(), providerId, currentRole);
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), providerId, currentRole);

        return new OAuthTokenResponse(accessToken, refreshToken);
    }

    public OAuthTokenResponse refreshAccessToken(String refreshTokenHeader) {
        String refreshToken = extractToken(refreshTokenHeader);

        // 토큰 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new CustomException(OAuthErrorCode.INVALID_TOKEN);
        }

        // 토큰 타입 검증
        if (!"refresh".equals(jwtProvider.getType(refreshToken))) {
            throw new CustomException(OAuthErrorCode.NOT_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        String providerId = jwtProvider.getProviderId(refreshToken);
        String role = jwtProvider.getRole(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(OAuthErrorCode.USER_NOT_FOUND));

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
        if (terms != null && !terms.isEmpty()) {
            List<com.example.RealMatch.user.domain.entity.enums.TermName> termNames = terms.stream()
                    .map(SignupCompleteRequest.TermAgreementDto::type)
                    .toList();

            java.util.Map<com.example.RealMatch.user.domain.entity.enums.TermName, Term> termMap = termRepository.findByNameIn(termNames).stream()
                    .collect(java.util.stream.Collectors.toMap(Term::getName, java.util.function.Function.identity()));

            if (termMap.size() != termNames.size()) {
                throw new CustomException(OAuthErrorCode.TERM_NOT_FOUND);
            }

            List<UserTerm> userTermsToSave = terms.stream().map(dto -> {
                Term term = termMap.get(dto.type());
                return UserTerm.builder()
                        .user(user)
                        .term(term)
                        .isAgreed(dto.agreed())
                        .build();
            }).toList();
            userTermRepository.saveAll(userTermsToSave);
        }
    }

    private void saveSignupPurposes(User user, List<Long> signupPurposeIds) {
        if (signupPurposeIds != null && !signupPurposeIds.isEmpty()) {
            List<SignupPurpose> purposes = signupPurposeRepository.findAllById(signupPurposeIds);
            if (purposes.size() != signupPurposeIds.size()) {
                throw new CustomException(OAuthErrorCode.PURPOSE_NOT_FOUND);
            }
            List<UserSignupPurpose> userPurposes = purposes.stream()
                    .map(purpose -> UserSignupPurpose.builder().user(user).purpose(purpose).build())
                    .toList();
            userSignupPurposeRepository.saveAll(userPurposes);
        }
    }

    private void saveContentCategories(User user, List<Long> contentCategoryIds) {
        if (contentCategoryIds != null && !contentCategoryIds.isEmpty()) {
            List<ContentCategory> categories = contentCategoryRepository.findAllById(contentCategoryIds);
            if (categories.size() != contentCategoryIds.size()) {
                throw new CustomException(OAuthErrorCode.CATEGORY_NOT_FOUND);
            }
            List<UserContentCategory> userContentCategories = categories.stream()
                    .map(category -> UserContentCategory.builder().user(user).contentCategory(category).build())
                    .toList();
            userContentCategoryRepository.saveAll(userContentCategories);
        }
    }
}
