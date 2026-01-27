package com.example.RealMatch.campaign.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.campaign.application.service.CampaignQueryService;
import com.example.RealMatch.campaign.presentation.dto.response.CampaignDetailResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name="Campaign", description = "캠페인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/campaigns")
public class CampaignController {

    private final CampaignQueryService campaignQueryService;

    @Operation(
            summary = "캠페인 상세 정보 조회 API by 박지영",
            description = """
                    캠페인 상세 정보를 조회합니다.
                    
                    formats : 형식, 
                    categories : 종류, 
                    tones : 톤, 
                    involvements : 관여도, 
                    usageRanges : 활용 범위 ,
                    """
    )
    @GetMapping("/{campaignId}")
    public CustomResponse<CampaignDetailResponse> getCampaignDetail(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long campaignId
    ) {
        return CustomResponse.ok(
                campaignQueryService.getCampaignDetail(campaignId)
        );
    }
}