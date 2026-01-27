package com.example.RealMatch.match.infrastructure.redis.document;

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
public class BrandTagDocument {

    @Id
    private String id;

    @Indexed
    private Long brandId;

    @Indexed
    private String brandName;

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
    private Set<String> preferredBodyTypes;  // 체형

    @Indexed
    private Set<String> preferredTopSizes;  // 상의 사이즈

    @Indexed
    private Set<String> preferredBottomSizes;  // 하의 사이즈

    // 컨텐츠 관련
    @Indexed
    private Long minContentsAverageViews;

    @Indexed
    private Long maxContentsAverageViews;

    @Indexed
    private Set<String> preferredContentsAges;  // 컨텐츠 시청 연령대

    @Indexed
    private Set<String> preferredContentsGenders;  // 컨텐츠 시청 성별

    @Indexed
    private Set<String> preferredContentsLengths;  // 컨텐츠 길이

    @Builder
    public BrandTagDocument(Long brandId, String brandName, Set<String> categories,
                            Set<String> preferredFashionTags, Set<String> preferredBeautyTags,
                            Set<String> preferredContentTags,
                            Integer minCreatorHeight, Integer maxCreatorHeight,
                            Set<String> preferredBodyTypes, Set<String> preferredTopSizes,
                            Set<String> preferredBottomSizes,
                            Long minContentsAverageViews, Long maxContentsAverageViews,
                            Set<String> preferredContentsAges, Set<String> preferredContentsGenders,
                            Set<String> preferredContentsLengths) {
        this.id = "brand:" + brandId;
        this.brandId = brandId;
        this.brandName = brandName;
        this.categories = categories;
        this.preferredFashionTags = preferredFashionTags;
        this.preferredBeautyTags = preferredBeautyTags;
        this.preferredContentTags = preferredContentTags;
        this.minCreatorHeight = minCreatorHeight;
        this.maxCreatorHeight = maxCreatorHeight;
        this.preferredBodyTypes = preferredBodyTypes;
        this.preferredTopSizes = preferredTopSizes;
        this.preferredBottomSizes = preferredBottomSizes;
        this.minContentsAverageViews = minContentsAverageViews;
        this.maxContentsAverageViews = maxContentsAverageViews;
        this.preferredContentsAges = preferredContentsAges;
        this.preferredContentsGenders = preferredContentsGenders;
        this.preferredContentsLengths = preferredContentsLengths;
    }
}
