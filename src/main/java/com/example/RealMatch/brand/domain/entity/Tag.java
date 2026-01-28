package com.example.RealMatch.brand.domain.entity;

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
@Table(name = "tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_parent_id", nullable = false)
    private BrandTagParent tagParent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(nullable = false, length = 100)
    private String name;

    @Builder
    public Tag(BrandTagParent tagParent, Brand brand, User user,
               String categoryName, String name) {
        this.tagParent = tagParent;
        this.brand = brand;
        this.user = user;
        this.categoryName = categoryName;
        this.name = name;
    }

}
