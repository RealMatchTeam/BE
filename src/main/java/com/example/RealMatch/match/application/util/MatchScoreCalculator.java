package com.example.RealMatch.match.application.util;

import java.util.Collections;
import java.util.Set;

import com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument;
import com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocument;
import com.example.RealMatch.match.infrastructure.redis.document.UserTagDocument;

public class MatchScoreCalculator {

    private static final double DISCRETE_TAG_WEIGHT = 0.6;
    private static final double CONTINUOUS_TAG_WEIGHT = 0.4;

    private MatchScoreCalculator() {
    }

    public static int calculateBrandMatchScore(UserTagDocument user, BrandTagDocument brand) {
        if (user == null || brand == null) {
            return 0;
        }

        double discreteScore = calculateDiscreteTagScore(user, brand);
        double continuousScore = calculateContinuousTagScore(user, brand);

        double totalScore = (discreteScore * DISCRETE_TAG_WEIGHT) + (continuousScore * CONTINUOUS_TAG_WEIGHT);
        return (int) Math.round(totalScore * 100);
    }

    public static int calculateCampaignMatchScore(UserTagDocument user, CampaignTagDocument campaign) {
        if (user == null || campaign == null) {
            return 0;
        }

        double discreteScore = calculateDiscreteTagScore(user, campaign);
        double continuousScore = calculateContinuousTagScore(user, campaign);

        double totalScore = (discreteScore * DISCRETE_TAG_WEIGHT) + (continuousScore * CONTINUOUS_TAG_WEIGHT);
        return (int) Math.round(totalScore * 100);
    }

    private static double calculateDiscreteTagScore(UserTagDocument user, BrandTagDocument brand) {
        double fashionScore = calculateSetMatchRatio(
                safeSet(user.getFashionTags()),
                safeSet(brand.getPreferredFashionTags())
        );
        double beautyScore = calculateSetMatchRatio(
                safeSet(user.getBeautyTags()),
                safeSet(brand.getPreferredBeautyTags())
        );
        double contentScore = calculateSetMatchRatio(
                safeSet(user.getContentTags()),
                safeSet(brand.getPreferredContentTags())
        );

        int count = 0;
        double sum = 0;

        if (!safeSet(brand.getPreferredFashionTags()).isEmpty()) {
            sum += fashionScore;
            count++;
        }
        if (!safeSet(brand.getPreferredBeautyTags()).isEmpty()) {
            sum += beautyScore;
            count++;
        }
        if (!safeSet(brand.getPreferredContentTags()).isEmpty()) {
            sum += contentScore;
            count++;
        }

        return count > 0 ? sum / count : 1.0;
    }

    private static double calculateDiscreteTagScore(UserTagDocument user, CampaignTagDocument campaign) {
        double fashionScore = calculateSetMatchRatio(
                safeSet(user.getFashionTags()),
                safeSet(campaign.getPreferredFashionTags())
        );
        double beautyScore = calculateSetMatchRatio(
                safeSet(user.getBeautyTags()),
                safeSet(campaign.getPreferredBeautyTags())
        );
        double contentScore = calculateSetMatchRatio(
                safeSet(user.getContentTags()),
                safeSet(campaign.getPreferredContentTags())
        );

        int count = 0;
        double sum = 0;

        if (!safeSet(campaign.getPreferredFashionTags()).isEmpty()) {
            sum += fashionScore;
            count++;
        }
        if (!safeSet(campaign.getPreferredBeautyTags()).isEmpty()) {
            sum += beautyScore;
            count++;
        }
        if (!safeSet(campaign.getPreferredContentTags()).isEmpty()) {
            sum += contentScore;
            count++;
        }

        return count > 0 ? sum / count : 1.0;
    }

    private static double calculateContinuousTagScore(UserTagDocument user, BrandTagDocument brand) {
        int totalConditions = 0;
        int matchedConditions = 0;

        // 키 조건
        if (brand.getMinCreatorHeight() != null || brand.getMaxCreatorHeight() != null) {
            totalConditions++;
            if (isInRange(user.getHeight(), brand.getMinCreatorHeight(), brand.getMaxCreatorHeight())) {
                matchedConditions++;
            }
        }

        // 체형 조건
        if (!safeSet(brand.getPreferredBodyTypeTags()).isEmpty()) {
            totalConditions++;
            if (user.getBodyType() != null) {
                try {
                    Integer bodyTypeTag = Integer.parseInt(user.getBodyType());
                    if (brand.getPreferredBodyTypeTags().contains(bodyTypeTag)) {
                        matchedConditions++;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // 상의 사이즈 조건
        if (!safeSet(brand.getPreferredTopSizeTags()).isEmpty()) {
            totalConditions++;
            if (user.getTopSize() != null && brand.getPreferredTopSizeTags().contains(user.getTopSize())) {
                matchedConditions++;
            }
        }

        // 하의 사이즈 조건
        if (!safeSet(brand.getPreferredBottomSizeTags()).isEmpty()) {
            totalConditions++;
            if (user.getBottomSize() != null && brand.getPreferredBottomSizeTags().contains(user.getBottomSize())) {
                matchedConditions++;
            }
        }

        // 평균 조회수 조건
        if (!safeSet(brand.getPreferredContentsAverageViewsTags()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getAverageContentsViews()), brand.getPreferredContentsAverageViewsTags())) {
                matchedConditions++;
            }
        }

        // 시청 연령대 조건
        if (!safeSet(brand.getPreferredContentsAgeTags()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getContentsAge()), brand.getPreferredContentsAgeTags())) {
                matchedConditions++;
            }
        }

        // 시청 성별 조건
        if (!safeSet(brand.getPreferredContentsGenderTags()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getContentsGender()), brand.getPreferredContentsGenderTags())) {
                matchedConditions++;
            }
        }

        // 컨텐츠 길이 조건
        if (!safeSet(brand.getPreferredContentsLengthTags()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getContentsLength()), brand.getPreferredContentsLengthTags())) {
                matchedConditions++;
            }
        }

        return totalConditions > 0 ? (double) matchedConditions / totalConditions : 1.0;
    }

    private static double calculateContinuousTagScore(UserTagDocument user, CampaignTagDocument campaign) {
        int totalConditions = 0;
        int matchedConditions = 0;

        // 키 조건
        if (campaign.getMinCreatorHeight() != null || campaign.getMaxCreatorHeight() != null) {
            totalConditions++;
            if (isInRange(user.getHeight(), campaign.getMinCreatorHeight(), campaign.getMaxCreatorHeight())) {
                matchedConditions++;
            }
        }

        // 체형 조건
        if (!safeSet(campaign.getPreferredBodyTypeTags()).isEmpty()) {
            totalConditions++;
            if (user.getBodyType() != null) {
                try {
                    Integer bodyTypeTag = Integer.parseInt(user.getBodyType());
                    if (campaign.getPreferredBodyTypeTags().contains(bodyTypeTag)) {
                        matchedConditions++;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // 상의 사이즈 조건
        if (!safeSet(campaign.getPreferredTopSizeTags()).isEmpty()) {
            totalConditions++;
            if (user.getTopSize() != null && campaign.getPreferredTopSizeTags().contains(user.getTopSize())) {
                matchedConditions++;
            }
        }

        // 하의 사이즈 조건
        if (!safeSet(campaign.getPreferredBottomSizeTags()).isEmpty()) {
            totalConditions++;
            if (user.getBottomSize() != null && campaign.getPreferredBottomSizeTags().contains(user.getBottomSize())) {
                matchedConditions++;
            }
        }

        // 평균 조회수 조건
        if (!safeSet(campaign.getPreferredContentsAverageViewsTags()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getAverageContentsViews()), campaign.getPreferredContentsAverageViewsTags())) {
                matchedConditions++;
            }
        }

        // 시청 연령대 조건
        if (!safeSet(campaign.getPreferredContentsAgeTags()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getContentsAge()), campaign.getPreferredContentsAgeTags())) {
                matchedConditions++;
            }
        }

        // 시청 성별 조건
        if (!safeSet(campaign.getPreferredContentsGenderTags()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getContentsGender()), campaign.getPreferredContentsGenderTags())) {
                matchedConditions++;
            }
        }

        // 컨텐츠 길이 조건
        if (!safeSet(campaign.getPreferredContentsLengthTags()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getContentsLength()), campaign.getPreferredContentsLengthTags())) {
                matchedConditions++;
            }
        }

        return totalConditions > 0 ? (double) matchedConditions / totalConditions : 1.0;
    }

    private static double calculateSetMatchRatio(Set<Integer> userTags, Set<Integer> preferredTags) {
        if (preferredTags.isEmpty()) {
            return 1.0;
        }
        if (userTags.isEmpty()) {
            return 0.0;
        }

        long matchCount = userTags.stream()
                .filter(preferredTags::contains)
                .count();

        return (double) matchCount / preferredTags.size();
    }

    private static <T extends Number & Comparable<T>> boolean isInRange(T value, T min, T max) {
        if (value == null) {
            return false;
        }
        boolean minOk = min == null || value.compareTo(min) >= 0;
        boolean maxOk = max == null || value.compareTo(max) <= 0;
        return minOk && maxOk;
    }

    private static boolean hasCommonElements(Set<Integer> set1, Set<Integer> set2) {
        return set1.stream().anyMatch(set2::contains);
    }

    private static <T> Set<T> safeSet(Set<T> set) {
        return set != null ? set : Collections.emptySet();
    }
}
