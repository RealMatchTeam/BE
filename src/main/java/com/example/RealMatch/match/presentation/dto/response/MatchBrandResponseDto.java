package com.example.RealMatch.match.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchBrandResponseDto {

    private Integer count;
    private List<BrandDto> brands;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandDto {
        private Long id;
        private String name;
        private Integer matchingRatio;
        private Boolean isLiked;
        private Boolean isRecruiting;
        private List<String> tags;
    }
}
