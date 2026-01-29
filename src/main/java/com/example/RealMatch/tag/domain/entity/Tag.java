package com.example.RealMatch.tag.domain.entity;

import com.example.RealMatch.global.common.DeleteBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tag_type", "tag_name", "tag_category"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_type", nullable = false)
    private String tagType;  // 뷰티, 패션, 콘텐츠

    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName; // 미니멀, 스킨케어, ...

    @Column(name = "tag_category", nullable = false)
    private String tagCategory; // 관심 스타일, 관심 기능, ...

    @Builder
    public Tag(
            String tagType,
            String tagName,
            String tagCategory
    ) {
        this.tagType = tagType;
        this.tagName = tagName;
        this.tagCategory = tagCategory;
    }
}

