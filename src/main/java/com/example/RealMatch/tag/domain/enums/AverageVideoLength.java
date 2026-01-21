package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AverageVideoLength {

    LESS_THAN_15_SEC(0, 14, "15초 미만"),
    FIFTEEN_TO_THIRTY_SEC(15, 29, "15초~30초"),
    THIRTY_TO_FORTY_FIVE_SEC(30, 44, "30초~45초"),
    FORTY_FIVE_TO_SIXTY_SEC(45, 59, "45초~60초"),
    OVER_SIXTY_SEC(60, Integer.MAX_VALUE, "60초 이상");

    private final int minSeconds;
    private final int maxSeconds;
    private final String description;

    public boolean contains(int seconds) {
        return seconds >= minSeconds && seconds <= maxSeconds;
    }
}
