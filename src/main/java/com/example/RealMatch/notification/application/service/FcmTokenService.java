package com.example.RealMatch.notification.application.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.notification.domain.entity.FcmToken;
import com.example.RealMatch.notification.domain.repository.FcmTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FcmTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(FcmTokenService.class);

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * FCM 토큰을 등록한다.
     * 동일한 토큰이 이미 존재하면 소유자를 현재 유저로 재할당한다 (디바이스 로그아웃→재로그인 대응).
     */
    public void registerToken(Long userId, String token, String deviceInfo) {
        Optional<FcmToken> existing = fcmTokenRepository.findByToken(token);

        if (existing.isPresent()) {
            FcmToken fcmToken = existing.get();
            if (!fcmToken.getUserId().equals(userId)) {
                fcmToken.reassignTo(userId);
                LOG.info("[FCM] Token reassigned. token={}..., newUserId={}", token.substring(0, 10), userId);
            }
            return;
        }

        FcmToken fcmToken = FcmToken.builder()
                .userId(userId)
                .token(token)
                .deviceInfo(deviceInfo)
                .build();
        fcmTokenRepository.save(fcmToken);
        LOG.info("[FCM] Token registered. userId={}, deviceInfo={}", userId, deviceInfo);
    }

    /**
     * FCM 토큰을 삭제한다 (로그아웃 시 호출).
     */
    public void removeToken(String token) {
        fcmTokenRepository.deleteByToken(token);
        LOG.info("[FCM] Token removed. token={}...", token.substring(0, Math.min(10, token.length())));
    }

    /**
     * 유저의 모든 FCM 토큰을 삭제한다 (회원 탈퇴 시 호출).
     */
    public void removeAllTokensByUserId(Long userId) {
        fcmTokenRepository.deleteByUserId(userId);
        LOG.info("[FCM] All tokens removed. userId={}", userId);
    }
}
