package com.example.RealMatch.user.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FavoriteBrandDto {

    private Long brandId;
    private String brandName;
    private String brandLogoUrl;

    private Long matchingRatio;     // 99, 98, 79 …
    private Long likeCount;         // 항상 true (찜 목록)

    private List<String> tags;       // #청정자극 #저자극 …

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
