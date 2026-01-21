package com.example.RealMatch.brand.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.global.common.BaseEntity;

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
@Table(name = "p_brand_like_read")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandLikeRead extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_read_id")
    private Long likeReadId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "like_num")
    private Integer likeNum;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public BrandLikeRead(Brand brand, Integer likeNum) {
        this.brand = brand;
        this.likeNum = likeNum;
        this.isDeleted = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLikeNum(Integer likeNum) {
        this.likeNum = likeNum;
        this.updatedAt = LocalDateTime.now();
    }
}
