package com.example.RealMatch.user.domain.entity;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.user.enums.DeleteType;
import com.example.RealMatch.user.enums.UserRole;
import com.example.RealMatch.user.enums.Gender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String name;

    private Gender gender;

    private LocalDate birth;

    @Column(nullable = false)
    private String nickname;

    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDate.now();
    }

    @Column(name = "deleted_at")
    private LocalDate deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "deleted_by")
    private DeleteType deletedBy;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "main_account")
    private String mainAccount;

    @Column(name = "sns_url")
    private String snsUrl;

    @Builder
    public User(String name,
                String nickname, UserRole role, Gender gender, LocalDate birth, String address, String detailAddress, String snsUrl) {
        this.name = name;
        this.nickname = nickname;
        this.role = role != null ? role : UserRole.CREATOR;
        this.gender = gender;
        this.birth = birth;
        this.address = address;
        this.detailAddress = detailAddress;
        this.snsUrl = snsUrl;
    }
}