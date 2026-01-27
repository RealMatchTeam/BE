package com.example.RealMatch.user.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.user.domain.entity.enums.Gender;
import com.example.RealMatch.user.domain.entity.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users",   uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_email_deleted", columnNames = {"email", "is_deleted"}),
        @UniqueConstraint(name = "uq_user_nickname_deleted", columnNames = {"nickname", "is_deleted"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    private LocalDate birth;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false, length = 255)
    private String email; // 최초 가입한 계정의 이메일

    @Column(length = 500)
    private String address;

    @Column(name = "detail_address", length = 500)
    private String detailAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Builder
    public User(String name, Gender gender, LocalDate birth, String nickname,
                String email, String address, String detailAddress, Role role,
                String profileImageUrl) {
        this.name = name;
        this.gender = gender;
        this.birth = birth;
        this.nickname = nickname;
        this.email = email;
        this.address = address;
        this.detailAddress = detailAddress;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public void updateProfile(String name, String nickname, String address, String detailAddress, String profileImageUrl) {
        this.name = name;
        this.nickname = nickname;
        this.address = address;
        this.detailAddress = detailAddress;
        this.profileImageUrl = profileImageUrl;
    }

    public void softDelete(Long deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    public void completeSignup(String nickname, LocalDate birth, Gender gender, Role role) {
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
        this.role = role; // 여기서 Role.GUEST가 Role.CREATOR 등으로 바뀝니다.
    }
}

