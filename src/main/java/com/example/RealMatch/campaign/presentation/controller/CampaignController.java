package com.example.RealMatch.campaign.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.campaign.application.service.CampaignLikeService;
import com.example.RealMatch.campaign.application.service.CampaignQueryService;
import com.example.RealMatch.campaign.presentation.dto.response.CampaignDetailResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Campaign", description = "캠페인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/campaigns")
public class CampaignController {

    private final CampaignQueryService campaignQueryService;
    private final CampaignLikeService campaignLikeService;

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
                campaignQueryService.getCampaignDetail(principal.getUserId(),campaignId)
        );
    }

    @Operation(
            summary = "캠페인 좋아요 / 좋아요 취소 API  by 박지영",
            description = """
                    캠페인에 좋아요를 누르거나,
                    이미 좋아요가 되어있다면 취소합니다.    
                    
                    /api/v1/campaigns/{campaignId}에서 like 값이 변경된 것을 확인해주세요.
                    """
    )
    @PostMapping("/{campaignId}/like")
    public CustomResponse<String> toggleCampaignLike(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long campaignId
    ) {
        boolean isLike = campaignLikeService.toggleLike(
                principal.getUserId(),
                campaignId
        );

        return CustomResponse.ok(
                isLike ? "캠페인 좋아요가 반영되었습니다." : "캠페인 좋아요가 취소되었습니다."
        );
    }

}
