package com.example.RealMatch.user.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.presentation.advice.ResourceNotFoundException;
import com.example.RealMatch.user.domain.entity.NotificationSetting;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.NotificationSettingRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.presentation.code.UserErrorCode;
import com.example.RealMatch.user.presentation.dto.request.NotificationUpdateRequestDto;
import com.example.RealMatch.user.presentation.dto.response.NotificationSettingResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;

    // 알림 설정 조회
    public List<NotificationSettingResponseDto> getNotificationSettings(Long userId) {
        List<NotificationSetting> settings = notificationSettingRepository.findByUserId(userId);

        return settings.stream()
                .map(NotificationSettingResponseDto::from)
                .collect(Collectors.toList());
    }

    // 알림 설정 변경 (없으면 생성, 있으면 수정)
    @Transactional
    public void updateNotificationSetting(Long userId, NotificationUpdateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(UserErrorCode.USER_NOT_FOUND));

        NotificationSetting setting = notificationSettingRepository
                .findByUserIdAndTypeAndChannel(userId, request.getType(), request.getChannel())
                .orElse(null);

        if (setting == null) {
            // 설정이 없으면 새로 생성
            setting = NotificationSetting.builder()
                    .user(user)
                    .type(request.getType())
                    .channel(request.getChannel())
                    .isEnabled(request.isEnabled())
                    .build();
            notificationSettingRepository.save(setting);
        } else {
            // 설정이 있으면 상태 변경
            if (request.isEnabled()) {
                setting.enable();
            } else {
                setting.disable();
            }
        }
    }
}
