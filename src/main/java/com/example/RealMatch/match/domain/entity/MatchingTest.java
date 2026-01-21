package com.example.RealMatch.match.domain.entity;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.match.domain.entity.enums.VideoLength;
import com.example.RealMatch.match.domain.entity.enums.ViewsRange;
import com.example.RealMatch.match.domain.entity.enums.WeightRange;
import com.example.RealMatch.user.domain.entity.User;

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
@Table(name = "p_matching_test")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingTest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Long height;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private WeightRange weight;

    @Column(name = "upper_size")
    private Long upperSize;

    @Column(name = "lower_size")
    private Long lowerSize;

    @Column(name = "sns_url", length = 500)
    private String snsUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_length", length = 20)
    private VideoLength videoLength;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ViewsRange views;

    @Column(name = "creator_type", length = 100)
    private String creatorType;

    @Column(name = "good_with", length = 255)
    private String goodWith;

    @Builder
    public MatchingTest(User user, Long height, WeightRange weight,
                        Long upperSize, Long lowerSize, String snsUrl,
                        VideoLength videoLength, ViewsRange views,
                        String creatorType, String goodWith) {
        this.user = user;
        this.height = height;
        this.weight = weight;
        this.upperSize = upperSize;
        this.lowerSize = lowerSize;
        this.snsUrl = snsUrl;
        this.videoLength = videoLength;
        this.views = views;
        this.creatorType = creatorType;
        this.goodWith = goodWith;
    }

    public void update(Long height, WeightRange weight, Long upperSize, Long lowerSize,
                       String snsUrl, VideoLength videoLength, ViewsRange views,
                       String creatorType, String goodWith) {
        this.height = height;
        this.weight = weight;
        this.upperSize = upperSize;
        this.lowerSize = lowerSize;
        this.snsUrl = snsUrl;
        this.videoLength = videoLength;
        this.views = views;
        this.creatorType = creatorType;
        this.goodWith = goodWith;
    }
}
