package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentsType {
    VLOG("브이로그"),
    REVIEW("리뷰"),
    GETREADYWITHME("겟레디윗미"),
    BEFOREAFTER("비포애프터"),
    STORY("스토리"),
    CHALLENGE("챌린지");

    private final String description;
}
