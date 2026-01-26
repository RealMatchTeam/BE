package com.example.RealMatch.tag.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_content_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "content_tag_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserContentTag extends BaseEntity {

    @Id
    @UuidGenerator
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_tag_id", nullable = false)
    private TagContent tagContent;

    @Builder
    public UserContentTag(
            Long userId,
            TagContent tagContent
    ) {
        this.userId = userId;
        this.tagContent = tagContent;
    }
}
