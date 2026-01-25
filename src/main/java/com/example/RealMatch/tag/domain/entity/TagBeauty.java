package com.example.RealMatch.tag.domain.entity;

import java.util.UUID;

import com.example.RealMatch.global.common.DeleteBaseEntity;
import com.example.RealMatch.tag.domain.enums.ContentTagType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "tag_content",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tag_type", "eng_name"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagBeauty extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type", nullable = false, length = 30)
    private ContentTagType tagType;

    @Column(name = "eng_name", nullable = false, length = 100)
    private String engName;

    @Column(name = "kor_name", nullable = false, length = 100)
    private String korName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    public TagBeauty(
            ContentTagType tagType,
            String engName,
            String korName,
            Integer displayOrder
    ) {
        this.tagType = tagType;
        this.engName = engName;
        this.korName = korName;
        this.displayOrder = displayOrder;
    }
}

