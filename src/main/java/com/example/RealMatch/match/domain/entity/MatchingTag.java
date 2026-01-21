package com.example.RealMatch.match.domain.entity;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.match.domain.entity.enums.TagType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "p_matching_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private MatchingTest test;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type", length = 30)
    private TagType tagType;

    @Builder
    public MatchingTag(MatchingTest test, TagType tagType) {
        this.test = test;
        this.tagType = tagType;
    }
}
