package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentsContribution {
    NONE("없음"),
    GUIDELINE("가이드라인만"),
    MOST("대부분"),
    ALL("전부");

    private final String description;
}
