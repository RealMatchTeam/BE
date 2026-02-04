package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagCategory {
    // BEAUTY
    BEAUTY_STYLE("관심 스타일"),
    SKIN_TYPE("피부 타입"),
    SKIN_CARE_MAIN_FUNCTION("관심 기능"),
    MAKEUP_STYLE("메이크업"),

    // FASHION
    FASHION_BRAND_TYPE("관심 브랜드 종류"),
    FASHION_STYLE("관심 스타일");

    private final String description;
}
