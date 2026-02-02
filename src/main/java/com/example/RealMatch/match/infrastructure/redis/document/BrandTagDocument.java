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
    private Set<Integer> preferredBodyTypeTags;  // 체형

    @Indexed
    private Set<Integer> preferredTopSizeTags;  // 상의 사이즈

    @Indexed
    private Set<Integer> preferredBottomSizeTags;  // 하의 사이즈

    // 컨텐츠 관련
    @Indexed
    private Set<Integer> preferredContentsAverageViewsTags;

    @Indexed
    private Set<Integer> preferredContentsAgeTags;  // 컨텐츠 시청 연령대

    @Indexed
    private Set<Integer> preferredContentsGenderTags;  // 컨텐츠 시청 성별

    @Indexed
    private Set<Integer> preferredContentsLengthTags;  // 컨텐츠 길이

    @Builder
    public BrandTagDocument(Long brandId, String brandName, Set<String> categories,
                            Set<Integer> preferredFashionTags, Set<Integer> preferredBeautyTags,
                            Set<Integer> preferredContentTags,
                            Integer minCreatorHeight, Integer maxCreatorHeight,
                            Set<Integer> preferredBodyTypeTags, Set<Integer> preferredTopSizeTags,
                            Set<Integer> preferredBottomSizeTags,
                            Set<Integer> preferredContentsAverageViewsTags,
                            Set<Integer> preferredContentsAgeTags, Set<Integer> preferredContentsGenderTags,
                            Set<Integer> preferredContentsLengthTags) {
        this.id = "brand:" + brandId;
        this.brandId = brandId;
        this.brandName = brandName;
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
    }
}
