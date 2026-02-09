package com.example.RealMatch.user.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FavoriteBrandListResponseDto {

    private Integer count;
    private List<FavoriteBrandDto> brands;

    public static FavoriteBrandListResponseDto empty() {
        return FavoriteBrandListResponseDto.builder()
                .count(0)
                .brands(List.of())
                .build();
    }
}
