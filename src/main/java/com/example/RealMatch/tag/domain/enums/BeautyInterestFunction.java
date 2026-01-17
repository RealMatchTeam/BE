package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BeautyInterestFunction {
    TROUBLE("트러블"),
    MOISTURE("보습"),
    SOOTHING("진정"),
    WHITENING("미백"),
    ANTIAGING("안티에이징"),
    EXFOLIATIONORPORTCARE("각질제거/모공케어");

    private final String description;
}
