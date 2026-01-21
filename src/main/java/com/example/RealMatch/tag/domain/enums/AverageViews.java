package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AverageViews {

    LESS_THAN_10K(0, 9_999, "1만 미만"),
    TEN_K_TO_100K(10_000, 99_999, "1만~10만"),
    HUNDRED_K_TO_500K(100_000, 499_999, "10만~50만"),
    FIVE_HUNDRED_K_TO_1M(500_000, 999_999, "50만~100만"),
    OVER_1M(1_000_000, Integer.MAX_VALUE, "100만 이상");

    private final int minViews;
    private final int maxViews;
    private final String description;

    public boolean contains(int views) {
        return views >= minViews && views <= maxViews;
    }
}
