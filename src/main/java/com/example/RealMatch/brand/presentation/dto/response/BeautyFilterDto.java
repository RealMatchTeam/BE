package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeautyFilterDto {
    private List<CategoryDto> category;
    private List<FunctionDto> function;
    private List<SkinTypeDto> skinType;
    private List<MakeUpStyleDto> makeUpStyle;
}
