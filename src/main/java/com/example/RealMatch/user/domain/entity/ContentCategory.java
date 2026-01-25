package com.example.RealMatch.user.domain.entity;

import java.util.UUID;

import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentCategory extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "category_name", nullable = false, length = 255)
    private String categoryName;

    @Builder
    public ContentCategory(String categoryName) {
        this.categoryName = categoryName;
    }
}
