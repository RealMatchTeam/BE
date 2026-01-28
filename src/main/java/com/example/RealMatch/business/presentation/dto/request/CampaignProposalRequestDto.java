package com.example.RealMatch.business.presentation.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CampaignProposalRequestDto {

    @NotNull
    private Long brandId;

    /**
     * 기존 캠페인 기반 제안이면 campaignId 필수
     * 신규 캠페인 제안이면 null
     */
    private Long campaignId;

    @NotBlank
    private String campaignName;

    @NotBlank
    private String description;

    @NotEmpty
    @Valid
    private List<CampaignContentTagRequest> formatTags;

    @NotEmpty
    @Valid
    private List<CampaignContentTagRequest> typeTags;

    @NotEmpty
    @Valid
    private List<CampaignContentTagRequest> toneTags;

    @NotEmpty
    @Valid
    private List<CampaignContentTagRequest> engagementTags;

    @NotEmpty
    @Valid
    private List<CampaignContentTagRequest> usageTags;

    @NotNull
    private Integer rewardAmount;

    @NotNull
    private Long productId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    public record CampaignContentTagRequest(
            @NotNull UUID tagId,
            String customValue
    ) {}
}
