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
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Business", description = "비즈니스 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/campaigns")
public class CampaignApplyController {

    private final CampaignApplyService campaignApplyService;
    private final CampaignApplyQueryService campaignApplyQueryService;

    @Operation(
            summary = "캠페인 지원 API by 박지영",
            description = """
                    해당 캠페인을 지원합니다. 
                    같은 캠페인에 중복으로 지원할 수 없습니다.
                    """
    )
    @PostMapping("/{campaignId}/apply")
    public CustomResponse<String> applyCampaign(
            @Parameter(description = "캠페인 ID", example = "1")
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

    @Operation(
            summary = "내가 지원한 캠페인 상세 조회 API by 박지영",
            description = """
                    내가 지원한 캠페인의 상세 조회입니다.
                    campaign_ID를 보내주세요.
                    
                    REVIEWING : 검토중   
                    MATCHED : 수락    
                    REJECTED : 거절   
                    """
    )
    @GetMapping("/{campaignId}/apply/me")
    public ResponseEntity<CampaignApplyDetailResponse> getMyApplyCampaignDetails(
            @Parameter(description = "캠페인 ID", example = "1")
            @PathVariable Long campaignId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        CampaignApplyDetailResponse response =
                campaignApplyQueryService.getMyApplyCampaignDetails(campaignId, principal.getUserId());

        return ResponseEntity.ok(response);
    }
}
