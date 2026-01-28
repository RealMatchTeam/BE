package com.example.RealMatch.brand.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkinTypeDto {
    private Integer skinId;
    private String skinName;
}
