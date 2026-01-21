package com.example.RealMatch.user.domain.entity;

import com.example.RealMatch.global.common.UpdateBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String providerId;  // 카카오 고유 ID

    @Column(nullable = false)
    private String provider;  // kakao

    private String email;
    private String name;

    @Column(nullable = false)
    private String role = "USER";

    @Builder
    public User(String providerId, String provider, String email, String name, String role) {
        this.providerId = providerId;
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.role = role != null ? role : "USER";
    }

    public void updateProfile(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
