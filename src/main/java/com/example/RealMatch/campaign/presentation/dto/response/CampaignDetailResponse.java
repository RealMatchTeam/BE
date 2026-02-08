package com.example.RealMatch.campaign.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.entity.CampaignContentTag;
import com.example.RealMatch.tag.domain.enums.ContentTagType;
import com.example.RealMatch.tag.presentation.dto.response.ContentTagResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampaignDetailResponse {

    private Long  campaignId;
    private String title;
    private String description;
    private String imageUrl;

    private String preferredSkills;
    private String schedule;
    private String videoSpec;

    private boolean isLike;

    // 협찬품 (수정 필요)
    private String product;

    // 원고료
    private Long rewardAmount;

    // 제작 기간
    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDateTime recruitStartDate;
    private LocalDateTime recruitEndDate;

    private Integer quota;

    private ContentTagResponse contentTags;

    public static CampaignDetailResponse from(
            Campaign campaign,
            String imageUrl,
            boolean isLike,
            List<CampaignContentTag> tags
    ) {
        return CampaignDetailResponse.builder()
                .campaignId(campaign.getId())
                .title(campaign.getTitle())
                .description(campaign.getDescription())
                .imageUrl(imageUrl)
                .preferredSkills(campaign.getPreferredSkills())
                .schedule(campaign.getSchedule())
                .videoSpec(campaign.getVideoSpec())
                .isLike(isLike)
                .product(campaign.getProduct())
                .rewardAmount(campaign.getRewardAmount())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .recruitStartDate(campaign.getRecruitStartDate())
                .recruitEndDate(campaign.getRecruitEndDate())
                .quota(campaign.getQuota())
                .contentTags(toContentTagResponse(tags))
                .build();
    }

    private static ContentTagResponse toContentTagResponse(
            List<CampaignContentTag> tags
    ) {
        Map<ContentTagType, List<ContentTagResponse.TagItemResponse>> map =
                new EnumMap<>(ContentTagType.class);

        for (CampaignContentTag tag : tags) {
            ContentTagType type = tag.getTagContent().getTagType();

            map.computeIfAbsent(type, k -> new ArrayList<>())
                    .add(toTagItem(tag));
        }

        return new ContentTagResponse(
                map.getOrDefault(ContentTagType.VIEWER_GENDER, List.of()),
                map.getOrDefault(ContentTagType.VIEWER_AGE, List.of()),
                map.getOrDefault(ContentTagType.AVG_VIDEO_LENGTH, List.of()),
                map.getOrDefault(ContentTagType.AVG_VIDEO_VIEWS, List.of()),
                map.getOrDefault(ContentTagType.FORMAT, List.of()),
                map.getOrDefault(ContentTagType.CATEGORY, List.of()),
                map.getOrDefault(ContentTagType.TONE, List.of()),
                map.getOrDefault(ContentTagType.INVOLVEMENT, List.of()),
                map.getOrDefault(ContentTagType.USAGE_RANGE, List.of())
        );
    }

    private static ContentTagResponse.TagItemResponse toTagItem(
            CampaignContentTag tag
    ) {
        return new ContentTagResponse.TagItemResponse(
                tag.getTagContent().getId(),
                resolveTagName(tag)
        );
    }

    private static String resolveTagName(CampaignContentTag tag) {
        String baseName = tag.getTagContent().getKorName();
        if (tag.getCustomTagValue() != null && !tag.getCustomTagValue().isBlank()) {
            return baseName + " (" + tag.getCustomTagValue() + ")";
        }
        return baseName;
    }


}
