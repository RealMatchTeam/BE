package com.example.RealMatch.match.infrastructure.redis.document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Document
public class CampaignTagDocument {

    @Id
    private String id;

    @Indexed
    private Long campaignId;

    @Indexed
    private String campaignName;

    @Indexed
    private String description;

    @Indexed
    private BigDecimal rewardAmount;

    @Indexed
    private LocalDateTime recruitEndDate;

    @Indexed
    private Set<String> categories;  // "FASHION", "BEAUTY" or both

    // ************ //
    // 이산형 태그 조건 //
    // ************ //
    @Indexed
    private Set<String> preferredFashionTags;

    @Indexed
    private Set<String> preferredBeautyTags;

    @Indexed
    private Set<String> preferredContentTags;

    // ************ //
    // 연속형 태그 조건 //
    // ************ //

    // 패션 관련
    @Indexed
    private Integer minCreatorHeight;

    @Indexed
    private Integer maxCreatorHeight;

    @Indexed
    private Set<String> preferredBodyTypes;

    @Indexed
    private Integer minCreatorTopSizes;

    @Indexed
    private Integer maxCreatorTopSizes;

    @Indexed
    private Integer minCreatorBottomSizes;

    @Indexed
    private Integer maxCreatorBottomSizes;

    // 컨텐츠 관련
    @Indexed
    private Long minContentsAverageViews;

    @Indexed
    private Long maxContentsAverageViews;

    @Indexed
    private Set<String> preferredContentsAges;

    @Indexed
    private Set<String> preferredContentsGenders;

    @Indexed
    private Set<String> preferredContentsLengths;

    // 캠페인 기본 정보
    @Indexed
    private LocalDate startDate;

    @Indexed
    private LocalDate endDate;

    @Indexed
    private Integer quota;

    @Builder
    public CampaignTagDocument(Long campaignId, String campaignName, String description,
                               BigDecimal rewardAmount, LocalDateTime recruitEndDate,
                               Set<String> categories,
                               Set<String> preferredFashionTags, Set<String> preferredBeautyTags,
                               Set<String> preferredContentTags,
                               Integer minCreatorHeight, Integer maxCreatorHeight,
                               Set<String> preferredBodyTypes,
                               Integer minCreatorTopSizes, Integer maxCreatorTopSizes,
                               Integer minCreatorBottomSizes, Integer maxCreatorBottomSizes,
                               Long minContentsAverageViews, Long maxContentsAverageViews,
                               Set<String> preferredContentsAges, Set<String> preferredContentsGenders,
                               Set<String> preferredContentsLengths,
                               LocalDate startDate, LocalDate endDate, Integer quota) {
        this.id = "campaign:" + campaignId;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.description = description;
        this.rewardAmount = rewardAmount;
        this.recruitEndDate = recruitEndDate;
        this.categories = categories;
        this.preferredFashionTags = preferredFashionTags;
        this.preferredBeautyTags = preferredBeautyTags;
        this.preferredContentTags = preferredContentTags;
        this.minCreatorHeight = minCreatorHeight;
        this.maxCreatorHeight = maxCreatorHeight;
        this.preferredBodyTypes = preferredBodyTypes;
        this.minCreatorTopSizes = minCreatorTopSizes;
        this.maxCreatorTopSizes = maxCreatorTopSizes;
        this.minCreatorBottomSizes = minCreatorBottomSizes;
        this.maxCreatorBottomSizes = maxCreatorBottomSizes;
        this.minContentsAverageViews = minContentsAverageViews;
        this.maxContentsAverageViews = maxContentsAverageViews;
        this.preferredContentsAges = preferredContentsAges;
        this.preferredContentsGenders = preferredContentsGenders;
        this.preferredContentsLengths = preferredContentsLengths;
        this.startDate = startDate;
        this.endDate = endDate;
        this.quota = quota;
    }
}
