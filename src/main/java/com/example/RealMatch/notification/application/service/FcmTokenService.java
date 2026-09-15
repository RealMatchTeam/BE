package com.example.RealMatch.notification.application.service;

import static com.example.RealMatch.global.presentation.code.GeneralErrorCode.BAD_REQUEST;
import static com.example.RealMatch.global.presentation.code.GeneralErrorCode.NOT_FOUND;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.notification.application.repository.FcmTokenRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FcmTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(FcmTokenService.class);

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository users;

    public void registerToken(Long userId, String token, String deviceInfo) {
        if (token == null || token.isBlank() || token.length() > 500 || deviceInfo != null && deviceInfo.length() > 255) {
            throw new CustomException(BAD_REQUEST);
        }
        users.findByIdForUpdate(userId).orElseThrow(() -> new CustomException(
                NOT_FOUND));
        fcmTokenRepository.deleteByUserIdAndLastSeenAtBefore(userId, LocalDateTime.now().minusDays(90));
        var existing = fcmTokenRepository.findByToken(token);
        if (existing.filter(t -> t.getUserId().equals(userId)).isEmpty() && fcmTokenRepository.countByUserId(userId) >= 10) {
            throw new CustomException(BAD_REQUEST);
        }
        fcmTokenRepository.upsert(userId, token, deviceInfo, LocalDateTime.now());
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
