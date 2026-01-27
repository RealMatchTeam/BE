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
    private Set<String> fashionTags;

    @Indexed
    private Set<String> beautyTags;

    @Indexed
    private Set<String> contentTags;

    // ************ //
    // 연속형 태그 조건 //
    // ************ //

    // 패션 관련
    @Indexed
    private Integer height;

    @Indexed
    private String bodyType;

    @Indexed
    private String topSize;

    @Indexed
    private String bottomSize;

    // 컨텐츠 관련
    @Indexed
    private Long averageContentsViews;

    @Indexed
    private Set<String> contentsAge;

    @Indexed
    private Set<String> contentsGender;

    @Indexed
    private String contentsLength;

    @Builder
    public UserTagDocument(Long userId, Set<String> fashionTags, Set<String> beautyTags,
                           Set<String> contentTags, Integer height, String bodyType,
                           String topSize, String bottomSize, Long averageContentsViews,
                           Set<String> contentsAge, Set<String> contentsGender, String contentsLength) {
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
