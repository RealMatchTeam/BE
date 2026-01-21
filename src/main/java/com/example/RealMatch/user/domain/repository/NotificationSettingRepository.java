package com.example.RealMatch.user.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.NotificationSettingEntity;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.entity.enums.NotificationType;

public interface NotificationSettingRepository extends JpaRepository<NotificationSettingEntity, Long> {

    List<NotificationSettingEntity> findByUserId(Long userId);

    Optional<NotificationSettingEntity> findByUserIdAndTypeAndChannel(Long userId, NotificationType type, NotificationChannel channel);

    List<NotificationSettingEntity> findByUserIdAndIsEnabled(Long userId, boolean isEnabled);
}
