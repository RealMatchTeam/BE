package com.example.RealMatch.match.presentation.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRequestDto {

    private BeautyDto beauty;
    private FashionDto fashion;
    private ContentDto content;

    // ******* //
    // 뷰티 정보 //
    // ******* //
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BeautyDto {
        private List<Integer> interestStyleTags;
        private List<Integer> prefferedFunctionTags;
        private Integer skinTypeTags;
        private Integer skinToneTags;
        private Integer makeupStyleTags;
    }

    // ******* //
    // 패션 정보 //
    // ******* //
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FashionDto {
        private List<Integer> interestStyleTags;
        private List<Integer> preferredItemTags;
        private List<Integer> preferredBrandTags;
        private Integer heightTag;
        private Integer weightTypeTag;
        private Integer topSizeTag;
        private Integer bottomSizeTag;
    }

    // ******** //
    // 컨텐츠 정보 //
    // ******** //
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentDto {
        private SnsDto sns;
        private List<Integer> typeTags;
        private List<Integer> toneTags;
        private List<Integer> prefferedInvolvementTags;
        private List<Integer> prefferedCoverageTags;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SnsDto {
        private String url;
        private MainAudienceDto mainAudience;
        private AverageAudienceDto averageAudience;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MainAudienceDto {
        private List<Integer> genderTags;
        private List<Integer> ageTags;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AverageAudienceDto {
        private List<Integer> videoLengthTags;
        private List<Integer> videoViewsTags;
    }
}
