package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagType {
    FASHION("패션"),
    BEAUTY("뷰티"),
    CONTENT("콘텐츠");

    private final String description;
}
