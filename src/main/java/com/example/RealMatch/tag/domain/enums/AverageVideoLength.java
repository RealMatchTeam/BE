package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AverageVideoLength {
    LESSTHAN15SEC("15초 미만"),
    FIFTEEN_TO_THIRTY_SEC("15초~30초"),
    THIRTY_TO_FORTYFIVE_SEC("30초~45초"),
    FORTYFIVE_TO_FIFTY_SEC("45초~50초");

    private final String description;
}
