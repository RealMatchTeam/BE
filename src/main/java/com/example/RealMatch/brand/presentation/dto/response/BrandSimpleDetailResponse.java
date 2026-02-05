package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "브랜드 요약 정보 응답")
public record BrandSimpleDetailResponse(
        @Schema(description = "브랜드 ID", example = "1")
        Long brandId,

        @Schema(description = "브랜드 이름", example = "레알패션")
        String brandName,

        @Schema(description = "브랜드 로고 이미지 URL", example = "https://s3.../logo.png")
        String brandImageUrl,

        @Schema(description = "브랜드 설명 태그 리스트", example = "[\"심플한\", \"트렌디한\"]")
        List<String> brandTags,

        @Schema(description = "로그인 유저와의 매칭률 (0~100)", example = "85")
        Integer matchingRate
) {
}