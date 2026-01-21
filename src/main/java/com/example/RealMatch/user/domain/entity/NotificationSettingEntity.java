package com.example.RealMatch.user.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.entity.enums.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_notification_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NotificationChannel channel;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public NotificationSettingEntity(UserEntity user, NotificationType type, NotificationChannel channel, boolean isEnabled) {
        this.user = user;
        this.type = type;
        this.channel = channel;
        this.isEnabled = isEnabled;
        this.createdAt = LocalDateTime.now();
    }

    public void enable() {
        this.isEnabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.isEnabled = false;
        this.updatedAt = LocalDateTime.now();
    }
}
