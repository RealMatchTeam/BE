package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AverageViews {
    ONE_TO_HUNDRED_THOUSAND("1~10만"),
    HUNDRED_THOUSAND_TO_FIVE_HUNDRED_THOUSAND("10만~50만"),
    FIVE_HUNDRED_THOUSAND_TO_ONE_MILLION("50만~100만"),
    OVER_ONE_MILLION("100만 이상");

    private final String description;
}
