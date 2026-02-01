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
    private Set<Integer> preferredFashionTags;

    @Indexed
    private Set<Integer> preferredBeautyTags;

    @Indexed
    private Set<Integer> preferredContentTags;

    // ************ //
    // 연속형 태그 조건 //
    // ************ //

    // 패션 관련
    @Indexed
    private Integer minCreatorHeight;

    @Indexed
    private Integer maxCreatorHeight;

    @Indexed
    private Set<Integer> preferredBodyTypeTags;

    @Indexed
    private Set<Integer> preferredTopSizeTags;

    @Indexed
    private Set<Integer> preferredBottomSizeTags;

    // 컨텐츠 관련
    @Indexed
    private Set<Integer> preferredContentsAverageViewsTags;

    @Indexed
    private Set<Integer> preferredContentsAgeTags;

    @Indexed
    private Set<Integer> preferredContentsGenderTags;

    @Indexed
    private Set<Integer> preferredContentsLengthTags;

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
                               Set<Integer> preferredFashionTags, Set<Integer> preferredBeautyTags,
                               Set<Integer> preferredContentTags,
                               Integer minCreatorHeight, Integer maxCreatorHeight,
                               Set<Integer> preferredBodyTypeTags,
                               Set<Integer> preferredTopSizeTags, Set<Integer> preferredBottomSizeTags,
                               Set<Integer> preferredContentsAverageViewsTags,
                               Set<Integer> preferredContentsAgeTags, Set<Integer> preferredContentsGenderTags,
                               Set<Integer> preferredContentsLengthTags,
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
        this.preferredBodyTypeTags = preferredBodyTypeTags;
        this.preferredTopSizeTags = preferredTopSizeTags;
        this.preferredBottomSizeTags = preferredBottomSizeTags;
        this.preferredContentsAverageViewsTags = preferredContentsAverageViewsTags;
        this.preferredContentsAgeTags = preferredContentsAgeTags;
        this.preferredContentsGenderTags = preferredContentsGenderTags;
        this.preferredContentsLengthTags = preferredContentsLengthTags;
        this.startDate = startDate;
        this.endDate = endDate;
        this.quota = quota;
    }
}
