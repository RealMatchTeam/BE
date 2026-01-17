package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BeautySkinBright {
    LESSTHAN17("17호 이하"),
    SEVENTEEN_TO_TWENTYONE("17~21호"),
    TWENTYONE_TO_TWENTYTHREE("21~23호"),
    MORETHAN23("23호 이상");

    private final String description;
}
