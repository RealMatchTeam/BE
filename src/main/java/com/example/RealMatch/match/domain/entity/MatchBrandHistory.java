package com.example.RealMatch.match.domain.entity;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.global.common.DeleteBaseEntity;
import com.example.RealMatch.user.domain.entity.User;

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
@Table(name = "match_brand_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchBrandHistory extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "matching_ratio")
    private Long matchingRatio;

    @Column(name = "is_deprecated", nullable = false)
    private Boolean isDeprecated = false;

    @Builder
    public MatchBrandHistory(User user, Brand brand, Long matchingRatio) {
        this.user = user;
        this.brand = brand;
        this.matchingRatio = matchingRatio;
        this.isDeprecated = false;
    }

    public void updateMatchingRatio(Long matchingRatio) {
        this.matchingRatio = matchingRatio;
    }

    public void deprecate() {
        this.isDeprecated = true;
    }
}
