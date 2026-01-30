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

        if (brand.getMinCreatorHeight() != null || brand.getMaxCreatorHeight() != null) {
            totalConditions++;
            if (isInRange(user.getHeight(), brand.getMinCreatorHeight(), brand.getMaxCreatorHeight())) {
                matchedConditions++;
            }
        }

        if (!safeSet(brand.getPreferredBodyTypes()).isEmpty()) {
            totalConditions++;
            if (user.getBodyType() != null && brand.getPreferredBodyTypes().contains(user.getBodyType())) {
                matchedConditions++;
            }
        }

        if (!safeSet(brand.getPreferredTopSizes()).isEmpty()) {
            totalConditions++;
            if (user.getTopSize() != null && brand.getPreferredTopSizes().contains(user.getTopSize())) {
                matchedConditions++;
            }
        }

        if (!safeSet(brand.getPreferredBottomSizes()).isEmpty()) {
            totalConditions++;
            if (user.getBottomSize() != null && brand.getPreferredBottomSizes().contains(user.getBottomSize())) {
                matchedConditions++;
            }
        }

        if (brand.getMinContentsAverageViews() != null || brand.getMaxContentsAverageViews() != null) {
            totalConditions++;
            if (isInRange(user.getAverageContentsViews(), brand.getMinContentsAverageViews(), brand.getMaxContentsAverageViews())) {
                matchedConditions++;
            }
        }

        if (!safeSet(brand.getPreferredContentsAges()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getContentsAge()), brand.getPreferredContentsAges())) {
                matchedConditions++;
            }
        }

        if (!safeSet(brand.getPreferredContentsGenders()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getContentsGender()), brand.getPreferredContentsGenders())) {
                matchedConditions++;
            }
        }

        if (!safeSet(brand.getPreferredContentsLengths()).isEmpty()) {
            totalConditions++;
            if (user.getContentsLength() != null && brand.getPreferredContentsLengths().contains(user.getContentsLength())) {
                matchedConditions++;
            }
        }

        return totalConditions > 0 ? (double) matchedConditions / totalConditions : 1.0;
    }

    private static double calculateContinuousTagScore(UserTagDocument user, CampaignTagDocument campaign) {
        int totalConditions = 0;
        int matchedConditions = 0;

        if (campaign.getMinCreatorHeight() != null || campaign.getMaxCreatorHeight() != null) {
            totalConditions++;
            if (isInRange(user.getHeight(), campaign.getMinCreatorHeight(), campaign.getMaxCreatorHeight())) {
                matchedConditions++;
            }
        }

        if (!safeSet(campaign.getPreferredBodyTypes()).isEmpty()) {
            totalConditions++;
            if (user.getBodyType() != null && campaign.getPreferredBodyTypes().contains(user.getBodyType())) {
                matchedConditions++;
            }
        }

        if (campaign.getMinContentsAverageViews() != null || campaign.getMaxContentsAverageViews() != null) {
            totalConditions++;
            if (isInRange(user.getAverageContentsViews(), campaign.getMinContentsAverageViews(), campaign.getMaxContentsAverageViews())) {
                matchedConditions++;
            }
        }

        if (!safeSet(campaign.getPreferredContentsAges()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getContentsAge()), campaign.getPreferredContentsAges())) {
                matchedConditions++;
            }
        }

        if (!safeSet(campaign.getPreferredContentsGenders()).isEmpty()) {
            totalConditions++;
            if (hasCommonElements(safeSet(user.getContentsGender()), campaign.getPreferredContentsGenders())) {
                matchedConditions++;
            }
        }

        if (!safeSet(campaign.getPreferredContentsLengths()).isEmpty()) {
            totalConditions++;
            if (user.getContentsLength() != null && campaign.getPreferredContentsLengths().contains(user.getContentsLength())) {
                matchedConditions++;
            }
        }

        return totalConditions > 0 ? (double) matchedConditions / totalConditions : 1.0;
    }

    private static double calculateSetMatchRatio(Set<String> userTags, Set<String> preferredTags) {
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

    private static boolean hasCommonElements(Set<String> set1, Set<String> set2) {
        return set1.stream().anyMatch(set2::contains);
    }

    private static <T> Set<T> safeSet(Set<T> set) {
        return set != null ? set : Collections.emptySet();
    }
}
