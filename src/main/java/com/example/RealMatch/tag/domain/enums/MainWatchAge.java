package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MainWatchAge {
    TEN_TO_TWENTY("10~20대"),
    TWENTY_TO_THIRTY("20~30대"),
    THIRTY_TO_FORTY("30~40대"),
    FORTY_TO_FIFTY("40~50대"),
    OVER_FIFTY("50대 이상");

    private final String description;
}
