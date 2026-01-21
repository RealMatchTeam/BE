package com.example.RealMatch.match.presentation.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MatchRequestDto {

    private String userId;
    private String brandId;
    private String sex;
    private Integer age;
    private Integer height;
    private Integer weight;
    private SizeDto size;
    private BeautyDto beauty;
    private FashionDto fashion;
    private SnsDto sns;

    @Getter
    @NoArgsConstructor
    public static class SizeDto {
        private Integer upper;
        private Integer bottom;
    }

    @Getter
    @NoArgsConstructor
    public static class BeautyDto {
        private List<String> interests;
        private List<String> functions;
        private String skinType;
        private String skinTone;
        private String makeupStyle;
    }

    @Getter
    @NoArgsConstructor
    public static class FashionDto {
        private List<String> styles;
        private List<String> items;
        private List<String> preferredBrands;
    }

    @Getter
    @NoArgsConstructor
    public static class SnsDto {
        private String url;
        private MainAudienceDto mainAudience;
        private ContentStyleDto contentStyle;
    }

    @Getter
    @NoArgsConstructor
    public static class MainAudienceDto {
        private List<String> sex;
        private List<String> age;
    }

    @Getter
    @NoArgsConstructor
    public static class ContentStyleDto {
        private String avgVideoLength;
        private String avgViews;
        private String format;
        private String type;
        private String contributionLevel;
        private String usageCoverage;
    }
}
