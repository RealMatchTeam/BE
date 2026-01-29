package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentTagType {

    FORMAT("형식"),
    CATEGORY("종류"),
    TONE("톤"),
    INVOLVEMENT("관여도"),
    USAGE_RANGE("활용 범위"),
    ETC("기타");

    private final String korName;
}