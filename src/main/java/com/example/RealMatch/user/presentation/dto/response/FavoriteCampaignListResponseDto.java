package com.example.RealMatch.user.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FavoriteCampaignListResponseDto {

    private Integer count;
    private List<FavoriteCampaignDto> campaigns;

    public static FavoriteCampaignListResponseDto empty() {
        return FavoriteCampaignListResponseDto.builder()
                .count(0)
                .campaigns(List.of())
                .build();
    }
}
