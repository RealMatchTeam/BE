package com.example.RealMatch.business.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.business.application.service.CampaignApplyQueryService;
import com.example.RealMatch.business.application.service.CampaignApplyService;
import com.example.RealMatch.business.presentation.dto.request.CampaignApplyRequest;
import com.example.RealMatch.business.presentation.dto.response.CampaignApplyDetailResponse;
import com.example.RealMatch.business.presentation.swagger.CampaignApplySwagger;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Business", description = "비즈니스 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/campaigns")
public class CampaignApplyController implements CampaignApplySwagger {

    private final CampaignApplyService campaignApplyService;
    private final CampaignApplyQueryService campaignApplyQueryService;

    @Override
    @PostMapping("/{campaignId}/apply")
    public CustomResponse<String> applyCampaign(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Validated @RequestBody CampaignApplyRequest request
    ) {
        campaignApplyService.applyCampaign(
                campaignId,
                principal.getUserId(),
                request.reason()
        );

        return CustomResponse.ok("정상적으로 신청되었습니다.");
    }

    @Override
    @GetMapping("/{campaignId}/apply/me")
    public ResponseEntity<CampaignApplyDetailResponse> getMyApplyCampaignDetails(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        CampaignApplyDetailResponse response =
                campaignApplyQueryService.getMyApplyCampaignDetails(campaignId, principal.getUserId());

        return ResponseEntity.ok(response);
    }
}
