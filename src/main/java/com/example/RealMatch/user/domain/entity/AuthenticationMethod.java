package com.example.RealMatch.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "authentication_methods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthenticationMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 유저와 연관관계

    @Column(nullable = false)
    private String provider; // KAKAO, NAVER, GOOGLE

    @Column(name = "provider_id", nullable = false)
    private String providerId; // 소셜 UID

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    private String email;

    @Builder
    public AuthenticationMethod(User user, String provider, String providerId, String email) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.createdAt = LocalDate.now();
    }
}