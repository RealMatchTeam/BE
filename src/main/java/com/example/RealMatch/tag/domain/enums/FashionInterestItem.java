package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FashionInterestItem {
    CLOTHES("의류"),
    BAG("가방"),
    SHOES("신발"),
    JEWELRY("주얼리"),
    FASHION_PROPS("패션소품");

    private final String description;
}
