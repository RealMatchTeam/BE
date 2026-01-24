package com.example.RealMatch.brand.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandLikeViewResponseDto {

    @JsonProperty("BrandIsLiked")
    private Boolean brandIsLiked;
}
