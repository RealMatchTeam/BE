package com.example.RealMatch.user.presentation.dto.response;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.UserMatchingDetail;

public record MyProfileCardResponseDto(
        String nickname,
        String profileImageUrl,
        String gender,
        int age,
        List<String> interests,
        String snsUrl,
        MatchingResult matchingResult,
        MyType myType,
        List<CampaignInfo> campaigns,
        long totalElements,
        int totalPages
) {

    // ✅ 서비스에서는 이것만 호출
    public static MyProfileCardResponseDto from(
            User user,
            UserMatchingDetail detail,
            List<String> interests,
            List<CampaignInfo> campaigns,
            long totalElements,
            int size
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int age = user.getBirth() != null
                ? Period.between(user.getBirth(), LocalDate.now()).getYears()
                : 0;

        return new MyProfileCardResponseDto(
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getGender() != null ? user.getGender().name() : "",
                age,
                interests,
                detail.getSnsUrl(),
                new MatchingResult(detail.getCreatorType()),
                new MyType(
                        toBeautyType(detail),
                        toFashionType(detail),
                        toContentsType(detail)
                ),
                campaigns,
                totalElements,
                totalPages
        );
    }

    /* ===================== 내부 변환 로직 ===================== */

    private static BeautyType toBeautyType(UserMatchingDetail d) {
        return new BeautyType(
                split(d.getSkinType()),
                d.getSkinBrightness(),
                split(d.getMakeupStyle()),
                split(d.getInterestCategories()),
                split(d.getInterestFunctions())
        );
    }

    private static FashionType toFashionType(UserMatchingDetail d) {
        return new FashionType(
                d.getHeight(),
                d.getBodyShape(),
                d.getTopSize(),
                d.getBottomSize(),
                split(d.getInterestFields()),
                split(d.getInterestStyles()),
                split(d.getInterestBrands())
        );
    }

    private static ContentsType toContentsType(UserMatchingDetail d) {
        return new ContentsType(
                split(d.getViewerGender()),
                split(d.getViewerAge()),
                d.getAvgVideoLength(),
                d.getAvgViews(),
                split(d.getContentFormats()),
                split(d.getContentTones()),
                split(d.getDesiredInvolvement()),
                split(d.getDesiredUsageScope())
        );
    }

    private static List<String> split(String value) {
        return (value == null || value.isBlank())
                ? List.of()
                : List.of(value.split(","));
    }

    /* ===================== 내부 record들 ===================== */

    public record MatchingResult(
            String creatorType
    ) {}

    public record MyType(
            BeautyType beautyType,
            FashionType fashionType,
            ContentsType contentsType
    ) {}

    public record BeautyType(
            List<String> skinType,
            String skinBrightness,
            List<String> makeupStyle,
            List<String> interestCategories,
            List<String> interestFunctions
    ) {}

    public record FashionType(
            String height,
            String bodyShape,
            String topSize,
            String bottomSize,
            List<String> interestFields,
            List<String> interestStyles,
            List<String> interestBrands
    ) {}

    public record ContentsType(
            List<String> viewerGender,
            List<String> viewerAge,
            String avgVideoLength,
            String avgViews,
            List<String> contentFormats,
            List<String> contentTones,
            List<String> desiredInvolvement,
            List<String> desiredUsageScope
    ) {}

    // ✅ 협업 타입 추가
    public record CampaignInfo(
            Long campaignId,
            Long brandId,
            String brandName,
            String campaignTitle,
            String collaborationType,  // "APPLIED", "RECEIVED", "SENT"
            String status,             // "REVIEWING", "MATCHED", "REJECTED"
            LocalDate endDate
    ) {}
}
