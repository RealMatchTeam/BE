package com.example.RealMatch.brand.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandFilterResponseDto {
    private List<SortOptionDto> sortOptions;
    private List<FilterTabDto> filterTabs;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortOptionDto {
        private String code;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterTabDto {
        private String tabCode;
        private String tabName;
        private List<OptionDto> options;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {
        private String code;
        private String name;
    }
}
