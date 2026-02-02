package com.example.RealMatch.business.presentation.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.RealMatch.business.presentation.dto.request.CampaignApplyRequest;
import com.example.RealMatch.business.presentation.dto.response.CampaignApplyDetailResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

public interface CampaignApplySwagger {

    @Operation(
            summary = "캠페인 지원 API by 박지영",
            description = """
                    해당 캠페인을 지원합니다.
                    같은 캠페인에 중복으로 지원할 수 없습니다.
                    """
    )
    CustomResponse<String> applyCampaign(
            @Parameter(description = "캠페인 ID", example = "1")
            @PathVariable Long campaignId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Validated @RequestBody CampaignApplyRequest request
    );

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
    ResponseEntity<CampaignApplyDetailResponse> getMyApplyCampaignDetails(
            @Parameter(description = "캠페인 ID", example = "1")
            @PathVariable Long campaignId,
            @AuthenticationPrincipal CustomUserDetails principal
    );
}
