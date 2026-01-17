package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentsFormat {
    INSTAGRAMHISTORY("인스타그램 스토리"),
    INSTAGRAMPOST("인스타그램 포스트"),
    INSTAGRAMREELS("인스타그램 릴스");

    private final String description;
}
