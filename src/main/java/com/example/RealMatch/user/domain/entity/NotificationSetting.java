package com.example.RealMatch.user.domain.entity;

import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "app_push_enabled", nullable = false)
    private boolean appPushEnabled;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Builder
    public NotificationSetting(
            User user,
            boolean appPushEnabled,
            boolean emailEnabled
    ) {
        this.user = user;
        this.appPushEnabled = appPushEnabled;
        this.emailEnabled = emailEnabled;
    }

    public void update(boolean appPushEnabled, boolean emailEnabled) {
        this.appPushEnabled = appPushEnabled;
        this.emailEnabled = emailEnabled;
    }
}