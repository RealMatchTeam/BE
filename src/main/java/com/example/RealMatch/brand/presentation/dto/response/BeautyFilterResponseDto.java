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
public class BeautyFilterResponseDto {

    private List<CategoryDto> category;
    private List<FunctionDto> function;
    private List<SkinTypeDto> skinType;
    private List<MakeUpStyleDto> makeUpStyle;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private Long categoryId;
        private String categoryName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionDto {
        private Long functionId;
        private String functionName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkinTypeDto {
        private Long skinId;
        private String skinName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MakeUpStyleDto {
        private Long makeUpId;
        private String makeUpName;
    }
}
