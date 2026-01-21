package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BeautyMakeupStyle {
    NATURAL("내추럴"),
    GLAMOROUS("화려한"),
    GLOWY("글로우"),
    MATTE("매트");

    private final String description;
}
