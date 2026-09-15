package com.example.RealMatch.user.application.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;
import com.example.RealMatch.notification.application.repository.FcmTokenRepository;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserWithdrawService {

    private final UserRepository userRepository;
    private final FcmTokenRepository tokens;
    private final ApplicationEventPublisher events;
    public record UserWithdrawn(Long userId) { }

    public void withdraw(Long userId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(GeneralErrorCode.NOT_FOUND));

        user.withdraw(userId);
        tokens.deleteByUserId(userId);
        events.publishEvent(new UserWithdrawn(userId));
    }
}
