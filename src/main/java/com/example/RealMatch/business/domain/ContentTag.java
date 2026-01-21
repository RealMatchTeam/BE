package com.example.RealMatch.business.domain;

import com.example.RealMatch.business.domain.enums.ContentTagType;
import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "content_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tag_type", "eng_name"})
        }
)
public class ContentTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type", nullable = false, length = 30)
    private ContentTagType tagType;

    @Column(name = "eng_name", nullable = false, length = 100)
    private String engName;

    @Column(name = "kor_name", nullable = false, length = 100)
    private String korName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    protected ContentTag() {
    }

    public ContentTag(
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

