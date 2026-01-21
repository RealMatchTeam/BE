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
public class MatchCampaignResponseDto {

    private Integer count;
    private List<CampaignDto> brands;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignDto {
        private Long id;
        private String name;
        private Integer matchingRatio;
        private Boolean isLiked;
        private Boolean isRecruiting;
        private Integer manuscriptFee;
        private String detail;
        private Integer dDay;
        private Integer totalRecruit;
        private Integer currentRecruit;
    }
}
