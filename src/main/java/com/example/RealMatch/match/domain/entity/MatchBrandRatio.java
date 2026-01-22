package com.example.RealMatch.match.domain.entity;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.global.common.DeleteBaseEntity;
import com.example.RealMatch.user.domain.entity.User;

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
@Table(name = "match_brand_ratio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchBrandRatio extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Long ratio;

    @Builder
    public MatchBrandRatio(Brand brand, User user, Long ratio) {
        this.brand = brand;
        this.user = user;
        this.ratio = ratio;
    }

    public void updateRatio(Long ratio) {
        this.ratio = ratio;
    }

}
