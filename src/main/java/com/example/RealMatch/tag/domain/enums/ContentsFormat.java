package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentsFormat {
    INSTAGRAM_HISTORY("인스타그램 스토리"),
    INSTAGRAM_POST("인스타그램 포스트"),
    INSTAGRAM_REELS("인스타그램 릴스");

    private final String description;
}
