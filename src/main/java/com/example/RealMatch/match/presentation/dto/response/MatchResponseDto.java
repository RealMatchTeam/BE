package com.example.RealMatch.match.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDto {

    private String createrType;
    private String createrBeautyType;
    private String createrFashionType;
    private String createrContentsType;
    private String createrFitBrand;
}
