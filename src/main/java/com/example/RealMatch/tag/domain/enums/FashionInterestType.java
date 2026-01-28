package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FashionInterestType {
    SPA("SPA"),
    VINTAGE("빈티지"),
    MID_PRICED_BRAND("중가 브랜드"),
    DESIGNER_BRAND("디자이너 브랜드"),
    LUXURY_BRAND("럭셔리 브랜드");

    private final String description;
}
