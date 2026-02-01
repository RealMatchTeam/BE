package com.example.RealMatch.business.presentation.dto.response;

import com.example.RealMatch.business.domain.enums.ProposalStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CampaignApplyDetailResponse {

    private Long campaignId;
    private String campaignTitle;
    private String campaignReason;
    private ProposalStatus proposalStatus;
}
