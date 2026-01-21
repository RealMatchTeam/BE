package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BeautySkinType {
    DRY("건성"),
    OIL("지성"),
    COMBINATION("복합성"),
    SENSITIVE("민감성");

    private final String description;
}
