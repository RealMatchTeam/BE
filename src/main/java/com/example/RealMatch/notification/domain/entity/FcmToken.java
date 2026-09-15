package com.example.RealMatch.notification.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fcm_token",
        indexes = {
                @Index(name = "idx_fcm_token_user_seen", columnList = "user_id,last_seen_at,id")
},
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fcm_token_value", columnNames = "token")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, length = 500,
            columnDefinition = "varchar(500) character set utf8mb4 collate utf8mb4_0900_bin")
    private String token;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    private LocalDateTime lastSeenAt;

    @Builder
    protected FcmToken(Long userId, String token, String deviceInfo) {
        this.userId = userId;
        this.token = token;
        this.deviceInfo = deviceInfo;
        this.lastSeenAt = LocalDateTime.now();
    }

    public void reassignTo(Long newUserId) {
        this.userId = newUserId;
    }
}
