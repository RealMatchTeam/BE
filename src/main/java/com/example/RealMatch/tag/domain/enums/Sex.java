package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Sex {
    MALE("남성"),
    FEMALE("여성"),
    NONE("없음");

    private final String description;
}
