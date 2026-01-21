package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BeautyInterestStyle {
    SKINCARE("스킨케어"),
    MAKEUP("메이크업"),
    PERFUME("향수"),
    BODY("바디"),
    HAIR("헤어");

    private final String description;
}
