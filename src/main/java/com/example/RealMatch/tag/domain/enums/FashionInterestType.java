package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FashionInterestType {
    SPA("SPA"),
    VINTAGE("빈티지"),
    MIDPRICEDBRAND("중가 브랜드"),
    DESIGNERBRAND("디자이너 브랜드"),
    LUXURYBRAND("럭셔리 브랜드");

    private final String description;
}
