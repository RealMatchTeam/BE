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
public class UserTagDocument {

    @Id
    private String id;

    @Indexed
    private Long userId;

    // ************ //
    // 이산형 태그 조건 //
    // ************ //
    @Indexed
    private Set<Integer> fashionTags;

    @Indexed
    private Set<Integer> beautyTags;

    @Indexed
    private Set<Integer> contentTags;

    // ************ //
    // 연속형 태그 조건 //
    // ************ //

    // 패션 관련
    @Indexed
    private Integer heightTag;

    @Indexed
    private Integer bodyTypeTag;

    @Indexed
    private Integer topSizeTag;

    @Indexed
    private Integer bottomSizeTag;

    // 컨텐츠 관련
    @Indexed
    private Set<Integer> averageContentsViewsTags;

    @Indexed
    private Set<Integer> contentsAgeTags;

    @Indexed
    private Set<Integer> contentsGenderTags;

    @Indexed
    private Set<Integer> contentsLengthTags;

    @Builder
    public UserTagDocument(Long userId, Set<Integer> fashionTags, Set<Integer> beautyTags,
                           Set<Integer> contentTags, Integer heightTag, Integer bodyTypeTag,
                           Integer topSizeTag, Integer bottomSizeTag, Set<Integer> averageContentsViewsTags,
                           Set<Integer> contentsAgeTags, Set<Integer> contentsGenderTags, Set<Integer> contentsLengthTags) {
        this.id = "user:" + userId;
        this.userId = userId;
        this.fashionTags = fashionTags;
        this.beautyTags = beautyTags;
        this.contentTags = contentTags;
        this.heightTag = heightTag;
        this.bodyTypeTag = bodyTypeTag;
        this.topSizeTag = topSizeTag;
        this.bottomSizeTag = bottomSizeTag;
        this.averageContentsViewsTags = averageContentsViewsTags;
        this.contentsAgeTags = contentsAgeTags;
        this.contentsGenderTags = contentsGenderTags;
        this.contentsLengthTags = contentsLengthTags;
    }
}
