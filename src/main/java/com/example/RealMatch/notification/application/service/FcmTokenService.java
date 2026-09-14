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

    public void registerToken(Long userId, String token, String deviceInfo) {
        Optional<FcmToken> existing = fcmTokenRepository.findByToken(token);

        if (existing.isPresent()) {
            FcmToken fcmToken = existing.get();
            if (!fcmToken.getUserId().equals(userId)) {
                fcmToken.reassignTo(userId);
                LOG.info("[FCM] Token reassigned. token={}..., newUserId={}", token.substring(0, Math.min(token.length(), 10)), userId);
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

    public void removeToken(Long userId, String token) {
        int deleted = fcmTokenRepository.deleteByUserIdAndToken(userId, token);
        LOG.info("[FCM] Token removal completed. userId={}, deleted={}", userId, deleted);
    }

    public void removeAllTokensByUserId(Long userId) {
        fcmTokenRepository.deleteByUserId(userId);
        LOG.info("[FCM] All tokens removed. userId={}", userId);
    }
}
