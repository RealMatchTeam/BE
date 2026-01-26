package com.example.RealMatch.tag.domain.entity;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.tag.domain.entity.TagBeauty;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
        name = "user_beauty_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "beauty_tag_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBeautyTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beauty_tag_id", nullable = false)
    private TagBeauty tagBeauty;

    @Builder
    public UserBeautyTag(
            User user,
            TagBeauty tagBeauty
    ) {
        this.user = user;
        this.tagBeauty = tagBeauty;
    }
}
