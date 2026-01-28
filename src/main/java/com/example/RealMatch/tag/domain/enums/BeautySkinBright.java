package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BeautySkinBright {

    LESS_THAN_17(0, 16, "17호 미만"),
    SEVENTEEN_TO_TWENTY(17, 20, "17~20호"),
    TWENTY_ONE_TO_TWENTY_THREE(21, 23, "21~23호"),
    MORE_THAN_23(24, Integer.MAX_VALUE, "23호 초과");

    private final int minTone;
    private final int maxTone;
    private final String description;

    public boolean contains(int tone) {
        return tone >= minTone && tone <= maxTone;
    }
}
