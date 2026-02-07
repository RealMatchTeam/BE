package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagCategory {

    // BEAUTY
    BEAUTY_INTEREST_STYLE("관심 스타일"),
    BEAUTY_INTEREST_FUNCTION("관심 기능"),
    BEAUTY_SKIN_TYPE("피부 타입"),
    BEAUTY_SKIN_BRIGHTNESS("피부 밝기"),
    BEAUTY_MAKEUP_STYLE("메이크업 스타일"),

    // FASHION
    FASHION_INTEREST_STYLE("관심 스타일"),
    FASHION_INTEREST_ITEM("관심 아이템/분야"),
    FASHION_INTEREST_TYPE("관심 브랜드 종류"),
    FASHION_BODY_HEIGHT("키"),
    FASHION_BODY_WEIGHT("체형"),
    FASHION_BODY_TOP("상의 사이즈"),
    FASHION_BODY_BOTTOM("하의 사이즈");


    private final String description;
}
