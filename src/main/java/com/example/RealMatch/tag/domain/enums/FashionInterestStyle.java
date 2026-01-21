package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FashionInterestStyle {
    MINIMAL("미니멀"),
    FEMININE("페미닌"),
    LOVELY("러블리"),
    BUSINESS_CASUAL("비즈니스캐주얼"),
    CASUAL("캐주얼"),
    STREET("스트릿");

    private final String description;
}
