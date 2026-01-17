package com.example.RealMatch.match.presentation.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MatchRequestDto {

    private String userId;
    private String brandId;
    private String sex;
    private Integer age;

    // === 뷰티 특성 ===
    private List<String> beautyInterestStyle;
    private List<String> beautyInterestFunction;
    private String beautySkinType;
    private String beautySkinBright;
    private String beautyMakeupStyle;

    // === 패션 특성 ===
    private List<String> fashionInterestStyle;
    private List<String> fashionInterestItem;
    private List<String> fashionInterestType;
    private Integer height;
    private Integer weight;
    private Integer upperSize;
    private Integer bottomSize;

    // === 콘텐츠 특성 ===
    private String snsUrl;
    private List<String> mainWatchSex;
    private List<String> mainWatchAge;
    private String averageVieoLength;
    private String averageViews;
    private String conentsFormat;
    private String contentsType;
    private String contentsContribution;
    private String contentsUseConverage;
}
