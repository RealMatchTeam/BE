package com.example.RealMatch.match.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.brand.domain.entity.BrandEntity;
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
@Table(name = "p_match_brand_ratio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchBrandRatioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private BrandEntity brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private Long ratio;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public MatchBrandRatioEntity(BrandEntity brand, UserEntity user, Long ratio) {
        this.brand = brand;
        this.user = user;
        this.ratio = ratio;
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
    }

    public void updateRatio(Long ratio) {
        this.ratio = ratio;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
