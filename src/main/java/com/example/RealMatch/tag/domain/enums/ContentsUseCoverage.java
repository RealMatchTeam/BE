package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentsUseCoverage {
    FIRST("1차 사용권"),
    SECOND("2차 사용권");

    private final String description;
}
