package com.example.RealMatch.user.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.user.domain.entity.NotificationSetting;
import com.example.RealMatch.user.domain.entity.Term;
import com.example.RealMatch.user.domain.entity.UserTerm;
import com.example.RealMatch.user.domain.entity.enums.TermName;
import com.example.RealMatch.user.domain.repository.NotificationSettingRepository;
import com.example.RealMatch.user.domain.repository.TermRepository;
import com.example.RealMatch.user.domain.repository.UserTermRepository;
import com.example.RealMatch.user.presentation.code.UserErrorCode;
import com.example.RealMatch.user.presentation.dto.request.NotificationSettingUpdateRequest;
import com.example.RealMatch.user.presentation.dto.response.NotificationSettingResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingService {

    private static final TermName MARKETING_TERM_NAME = TermName.MARKETING_CONSENT;

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserTermRepository userTermRepository;
    private final TermRepository termRepository;

    /**
     * 내 알림 설정 조회
     */
    @Transactional(readOnly = true)
    public NotificationSettingResponse getMySetting(Long userId) {

        NotificationSetting setting = notificationSettingRepository
                .findOneByUserId(userId)
                .orElseThrow(() ->
                        new CustomException(UserErrorCode.USER_NOTIFICATION_SETTING_NOT_FOUND)
                );

        boolean marketingConsent = userTermRepository
                .findByUserIdAndTermName(userId, MARKETING_TERM_NAME)
                .isPresent(); // ⭐ 존재 여부로 판단

        return NotificationSettingResponse.builder()
                .marketingConsent(marketingConsent)
                .appPushEnabled(setting.isAppPushEnabled())
                .emailEnabled(setting.isEmailEnabled())
                .build();
    }

    /**
     * 내 알림 설정 수정 (설정 완료)
     */
    public void updateSetting(Long userId, NotificationSettingUpdateRequest request) {

        // 1️⃣ 알림 설정 업데이트
        NotificationSetting setting = notificationSettingRepository
                .findOneByUserId(userId)
                .orElseThrow(() ->
                        new CustomException(UserErrorCode.USER_NOTIFICATION_SETTING_NOT_FOUND)
                );

        setting.update(
                request.isAppPushEnabled(),
                request.isEmailEnabled()
        );

        Optional<UserTerm> optionalUserTerm =
                userTermRepository.findByUserIdAndTermName(userId, MARKETING_TERM_NAME);

        boolean wantMarketingConsent = request.isMarketingConsent();

        // Case 1: 동의 안 함 → 기존 row 있으면 삭제
        if (!wantMarketingConsent) {
            optionalUserTerm.ifPresent(userTermRepository::delete);
            return;
        }

        // Case 2: 동의 함 → 기존 row 없으면 생성
        if (optionalUserTerm.isEmpty()) {
            Term marketingTerm = termRepository
                    .findByName(MARKETING_TERM_NAME)
                    .orElseThrow(() ->
                            new CustomException(UserErrorCode.INVALID_TERM)
                    );

            UserTerm newUserTerm = UserTerm.builder()
                    .user(setting.getUser())
                    .term(marketingTerm)
                    .isAgreed(true)
                    .build();

            userTermRepository.save(newUserTerm);
        }
    }

}
