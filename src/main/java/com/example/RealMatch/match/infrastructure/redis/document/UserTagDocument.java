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
    private Integer height;

    @Indexed
    private String bodyType;

    @Indexed
    private Integer topSize;

    @Indexed
    private Integer bottomSize;

    // 컨텐츠 관련
    @Indexed
    private Set<Integer> averageContentsViews;

    @Indexed
    private Set<Integer> contentsAge;

    @Indexed
    private Set<Integer> contentsGender;

    @Indexed
    private Set<Integer> contentsLength;

    @Builder
    public UserTagDocument(Long userId, Set<Integer> fashionTags, Set<Integer> beautyTags,
                           Set<Integer> contentTags, Integer height, String bodyType,
                           Integer topSize, Integer bottomSize, Set<Integer> averageContentsViews,
                           Set<Integer> contentsAge, Set<Integer> contentsGender, Set<Integer> contentsLength) {
        this.id = "user:" + userId;
        this.userId = userId;
        this.fashionTags = fashionTags;
        this.beautyTags = beautyTags;
        this.contentTags = contentTags;
        this.height = height;
        this.bodyType = bodyType;
        this.topSize = topSize;
        this.bottomSize = bottomSize;
        this.averageContentsViews = averageContentsViews;
        this.contentsAge = contentsAge;
        this.contentsGender = contentsGender;
        this.contentsLength = contentsLength;
    }
}
