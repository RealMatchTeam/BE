package com.example.RealMatch.brand.domain.entity;

import com.example.RealMatch.global.common.DeleteBaseEntity;

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
@Table(name = "brand_like_read")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandLikeRead extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_read_id")
    private Long likeReadId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "like_num")
    private Integer likeNum;

    @Builder
    public BrandLikeRead(Brand brand, Integer likeNum) {
        this.brand = brand;
        this.likeNum = likeNum;
    }

    public void updateLikeNum(Integer likeNum) {
        this.likeNum = likeNum;
    }
}
