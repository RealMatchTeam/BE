package com.example.RealMatch.business.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.business.application.service.CampaignProposalQueryService;
import com.example.RealMatch.business.application.service.CampaignProposalService;
import com.example.RealMatch.business.presentation.dto.request.CampaignProposalRejectRequest;
import com.example.RealMatch.business.presentation.dto.request.CampaignProposalRequestDto;
import com.example.RealMatch.business.presentation.dto.response.CampaignProposalDetailResponse;
import com.example.RealMatch.business.presentation.swagger.CampaignProposalSwagger;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Business", description = "비즈니스 API")
@RestController
@RequestMapping("/api/v1/campaigns/proposal")
@RequiredArgsConstructor
public class CampaignProposalController implements CampaignProposalSwagger {
    private final CampaignProposalService campaignProposalService;
    private final CampaignProposalQueryService campaignProposalQueryService;

    @Override
    @PostMapping("/request")
    public CustomResponse<String> createCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CampaignProposalRequestDto request
    ) {
        campaignProposalService.createCampaignProposal(userDetails, request);
        return CustomResponse.ok("캠페인 제안에 성공했습니다.");
    }

    @Override
    @PostMapping("/{campaignProposalID}/re-request")
    public CustomResponse<String> modifyCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long campaignProposalID,
            @RequestBody @Valid CampaignProposalRequestDto request
    ) {
        campaignProposalService.modifyCampaignProposal(userDetails, campaignProposalID, request);
        return CustomResponse.ok("캠페인 제안을 수정했습니다.");
    }

    @Override
    @GetMapping("/{campaignProposalId}")
    public CustomResponse<CampaignProposalDetailResponse> getProposalDetail(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long campaignProposalId
    ) {
        CampaignProposalDetailResponse response =
                campaignProposalQueryService.getProposalDetail(
                        principal.getUserId(),
                        campaignProposalId
                );

        return CustomResponse.ok(response);
    }

    @Override
    @PatchMapping("/{campaignProposalId}/approve")
    public CustomResponse<String> approveCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long campaignProposalId
    ) {
        campaignProposalService.approveCampaignProposal(
                userDetails.getUserId(),
                campaignProposalId
        );

        return CustomResponse.ok("캠페인 제안을 수락했습니다.");
    }

    @Override
    @PatchMapping("/{campaignProposalId}/reject")
    public CustomResponse<String> rejectCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long campaignProposalId,
            @RequestBody(required = false) @Valid CampaignProposalRejectRequest request
    ) {
        campaignProposalService.rejectCampaignProposal(
                userDetails.getUserId(),
                campaignProposalId,
                request != null ? request.getRejectReason() : null
        );

        return CustomResponse.ok("캠페인 제안을 거절했습니다.");
    }

    @Override
    @PatchMapping("/{campaignProposalId}/cancel")
    public CustomResponse<String> cancelCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long campaignProposalId
    ) {
        campaignProposalService.cancelCampaignProposal(
                userDetails.getUserId(),
                campaignProposalId
        );

        return CustomResponse.ok("캠페인 제안을 취소했습니다.");
    }




}
