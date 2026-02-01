package com.example.RealMatch.campaign.presentation.dto.response;

import com.example.RealMatch.campaign.domain.entity.CampaignContentTag;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentTagResponse {

    private Long tagId;
    private String tagType;      // FORMAT, CATEGORY, TONE ...
    private String engName;
    private String korName;

    // ETC일 경우만 값 존재
    private String customValue;

    public static ContentTagResponse from(CampaignContentTag tag) {
        return ContentTagResponse.builder()
                .tagId(tag.getTagContent().getId())
                .tagType(tag.getTagContent().getTagType().name())
                .engName(tag.getTagContent().getEngName())
                .korName(tag.getTagContent().getKorName())
                .customValue(tag.getCustomTagValue())
                .build();
    }
}

