package com.example.RealMatch.campaign.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.entity.CampaignContentTag;
import com.example.RealMatch.tag.presentation.dto.response.TagResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampaignDetailResponse {

    private Long campaignId;
    private String title;
    private String description;
    private String preferredSkills;
    private String schedule;
    private String videoSpec;

    private String product;

    private Long rewardAmount;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDateTime recruitStartDate;
    private LocalDateTime recruitEndDate;

    private Integer quota;

    private TagResponse contentTags;

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
                .contentTags(toTagResponse(tags))
                .build();
    }

    private static TagResponse toTagResponse(List<CampaignContentTag> tags) {
        Map<String, List<TagResponse.TagItem>> categories = new HashMap<>();

        for (CampaignContentTag contentTag : tags) {
            String category = contentTag.getTag().getTagCategory();
            String tagName = resolveTagName(contentTag);

            categories.computeIfAbsent(category, k -> new ArrayList<>())
                    .add(new TagResponse.TagItem(
                            contentTag.getTag().getId(),
                            tagName
                    ));
        }

        String tagType = tags.isEmpty() ? null : tags.get(0).getTag().getTagType();
        return new TagResponse(tagType, categories);
    }

    private static String resolveTagName(CampaignContentTag tag) {
        String baseName = tag.getTag().getTagName();
        if (tag.getCustomTagValue() != null && !tag.getCustomTagValue().isBlank()) {
            return baseName + " (" + tag.getCustomTagValue() + ")";
        }
        return baseName;
    }
}
