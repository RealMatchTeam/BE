package com.example.RealMatch.user.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.NotificationSetting;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.entity.enums.NotificationType;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    List<NotificationSetting> findByUserId(Long userId);

    Optional<NotificationSetting> findByUserIdAndTypeAndChannel(Long userId, NotificationType type, NotificationChannel channel);

    List<NotificationSetting> findByUserIdAndIsEnabled(Long userId, boolean isEnabled);
}
