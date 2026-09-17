package com.example.RealMatch.oauth.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.config.jwt.JwtProvider;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.oauth.code.OAuthErrorCode;
import com.example.RealMatch.oauth.dto.IssuedTokens;
import com.example.RealMatch.oauth.dto.request.SignupCompleteRequest;
import com.example.RealMatch.oauth.token.RefreshTokenStore;
import com.example.RealMatch.user.application.util.NicknameValidator;
import com.example.RealMatch.user.domain.entity.ContentCategory;
import com.example.RealMatch.user.domain.entity.NotificationSetting;
import com.example.RealMatch.user.domain.entity.SignupPurpose;
import com.example.RealMatch.user.domain.entity.Term;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.UserContentCategory;
import com.example.RealMatch.user.domain.entity.UserSignupPurpose;
import com.example.RealMatch.user.domain.entity.UserTerm;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.entity.enums.TermName;
import com.example.RealMatch.user.domain.repository.ContentCategoryRepository;
import com.example.RealMatch.user.domain.repository.NotificationSettingRepository;
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
    private final NicknameValidator nicknameValidator;
    private final NotificationSettingRepository notificationSettingRepository;
    private final RefreshTokenStore refreshTokenStore;

    public IssuedTokens completeSignup(
            Long userId,
            String providerId,
            SignupCompleteRequest request,
            Optional<String> currentRefreshToken
    ) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(OAuthErrorCode.USER_NOT_FOUND));

        // GUEST가 아니면 이미 가입 완료된 유저
        if (!user.getRole().equals(Role.GUEST)) {
            throw new CustomException(OAuthErrorCode.ALREADY_SIGNED_UP);
        }

        // ⭐ 닉네임 중복 체크 추가
        nicknameValidator.validate(request.nickname());

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

        // 마케팅 알림 동의 시 알림 설정 처리
        handleNotificationSettings(user, request.terms());

        // 역할이 GUEST -> CREATOR/BRAND 로 바뀌었으므로 GUEST 리프레시 토큰은 폐기하고 새로 발급
        currentRefreshToken.ifPresent(this::revoke);

        return issueTokens(user, providerId);
    }

    /**
     * 소셜 로그인 성공 직후 토큰 발급. 리프레시 토큰의 jti 를 Redis 에 등록한다.
     */
    public IssuedTokens issueTokens(Long userId, String providerId, String role, String email) {
        String accessToken = jwtProvider.createAccessToken(userId, providerId, role, email);
        String refreshToken = jwtProvider.createRefreshToken(userId, providerId, role, email);

        refreshTokenStore.save(
                userId,
                jwtProvider.getJti(refreshToken),
                Duration.ofMillis(jwtProvider.getRefreshTokenExpireMillis())
        );
        return new IssuedTokens(accessToken, refreshToken);
    }

    private IssuedTokens issueTokens(User user, String providerId) {
        return issueTokens(user.getId(), providerId, user.getRole().name(), user.getEmail());
    }

    /**
     * 리프레시 토큰 로테이션: 기존 토큰을 소비(삭제)하고 액세스/리프레시 토큰을 모두 새로 발급한다.
     * 이미 소비된(혹은 로그아웃된) 토큰이면 탈취/재사용으로 간주하고 거부한다.
     */
    public IssuedTokens refresh(String refreshToken) {
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

        if (!refreshTokenStore.consume(userId, jwtProvider.getJti(refreshToken))) {
            throw new CustomException(OAuthErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(OAuthErrorCode.USER_NOT_FOUND));

        // role 은 토큰이 아니라 DB 기준으로 다시 읽어 최신 권한을 반영한다
        return issueTokens(user, providerId);
    }

    /**
     * 로그아웃: 저장된 jti 를 지워 해당 리프레시 토큰을 즉시 무효화한다.
     * 토큰이 이미 만료/변조된 경우에도 예외 없이 조용히 넘어간다 (로그아웃은 항상 성공해야 함).
     */
    public void revoke(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken) || !"refresh".equals(jwtProvider.getType(refreshToken))) {
            return;
        }
        refreshTokenStore.consume(jwtProvider.getUserId(refreshToken), jwtProvider.getJti(refreshToken));
    }

    private void saveTermAgreements(User user, List<SignupCompleteRequest.TermAgreementDto> terms) {
        // 요청 데이터 존재 여부 확인
        if (terms == null || terms.isEmpty()) {
            throw new CustomException(OAuthErrorCode.TERM_NOT_FOUND);
        }

        // 클라이언트가 보낸 약관 데이터를 Map으로 변환 (조회 최적화)
        java.util.Map<com.example.RealMatch.user.domain.entity.enums.TermName, Boolean> termAgreedMap =
                terms.stream().collect(java.util.stream.Collectors.toMap(
                        SignupCompleteRequest.TermAgreementDto::type,
                        SignupCompleteRequest.TermAgreementDto::agreed
                ));

        // DB에서 '필수(isRequired=true)'인 약관 목록을 가져와 검증
        List<Term> requiredTermsFromDb = termRepository.findByIsRequired(true);
        if (requiredTermsFromDb.stream()
                .anyMatch(term -> !termAgreedMap.getOrDefault(term.getName(), false))) {
            throw new CustomException(OAuthErrorCode.REQUIRED_TERM_NOT_AGREED);
        }

        // DB에서 요청된 약관 엔티티들을 모두 조회
        List<com.example.RealMatch.user.domain.entity.enums.TermName> requestedNames = new java.util.ArrayList<>(termAgreedMap.keySet());

        List<Term> allMatchingTerms = termRepository.findByNameIn(requestedNames);

        // DB에 존재하지 않는 약관 이름이 포함된 경우
        if (allMatchingTerms.size() != terms.size()) {
            throw new CustomException(OAuthErrorCode.TERM_NOT_FOUND);
        }

        // UserTerm 엔티티 생성 및 저장
        List<UserTerm> userTermsToSave = allMatchingTerms.stream()
                .map(term -> UserTerm.builder()
                        .user(user)
                        .term(term)
                        .isAgreed(termAgreedMap.get(term.getName()))
                        .build())
                .toList();

        userTermRepository.saveAll(userTermsToSave);
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

    /**
     * 마케팅 알림 동의 시 알림 설정 생성/업데이트
     */
    private void handleNotificationSettings(
            User user,
            List<SignupCompleteRequest.TermAgreementDto> terms
    ) {
        boolean marketingNotificationAgreed = terms.stream()
                .anyMatch(term ->
                        term.type() == TermName.MARKETING_NOTIFICATION
                                && term.agreed()
                );

        NotificationSetting notificationSetting =
                notificationSettingRepository.findByUserId(user.getId())
                        .orElseGet(() -> NotificationSetting.builder()
                                .user(user)
                                .build());

        notificationSetting.update(
                marketingNotificationAgreed,
                marketingNotificationAgreed
        );

        notificationSettingRepository.save(notificationSetting);
    }
}
