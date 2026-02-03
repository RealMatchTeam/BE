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

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampaignDetailResponse {

    private Long  campaignId;
    private String title;
    private String description;
    private String preferredSkills;
    private String schedule;
    private String videoSpec;

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

    private CampaignTagResponse contentTags;

    public static CampaignDetailResponse from(
            Campaign campaign,
            List<CampaignContentTag> tags
    ) {
        return CampaignDetailResponse.builder()
                .campaignId(campaign.getId())
                .title(campaign.getTitle())
                .description(campaign.getDescription())
                .preferredSkills(campaign.getPreferredSkills())
                .schedule(campaign.getSchedule())
                .videoSpec(campaign.getVideoSpec())
                .product(campaign.getProduct())
                .rewardAmount(campaign.getRewardAmount())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .recruitStartDate(campaign.getRecruitStartDate())
                .recruitEndDate(campaign.getRecruitEndDate())
                .quota(campaign.getQuota())
                .contentTags(toCampaignTagResponse(tags))
                .build();
    }

    private static CampaignTagResponse toCampaignTagResponse(
            List<CampaignContentTag> tags
    ) {
        Map<ContentTagType, List<CampaignTagResponse.TagItemResponse>> map =
                new EnumMap<>(ContentTagType.class);

        for (CampaignContentTag tag : tags) {
            ContentTagType type = tag.getTagContent().getTagType();

            map.computeIfAbsent(type, k -> new ArrayList<>())
                    .add(toTagItem(tag));
        }

        return new CampaignTagResponse(
                map.getOrDefault(ContentTagType.FORMAT, List.of()),
                map.getOrDefault(ContentTagType.CATEGORY, List.of()),
                map.getOrDefault(ContentTagType.TONE, List.of()),
                map.getOrDefault(ContentTagType.INVOLVEMENT, List.of()),
                map.getOrDefault(ContentTagType.USAGE_RANGE, List.of())
        );
    }

    private static CampaignTagResponse.TagItemResponse toTagItem(
            CampaignContentTag tag
    ) {
        return new CampaignTagResponse.TagItemResponse(
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
