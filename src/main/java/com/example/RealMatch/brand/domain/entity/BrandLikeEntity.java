package com.example.RealMatch.brand.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.user.domain.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "p_brand_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private BrandEntity brand;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public BrandLikeEntity(UserEntity user, BrandEntity brand) {
        this.user = user;
        this.brand = brand;
        this.createdAt = LocalDateTime.now();
    }
}
