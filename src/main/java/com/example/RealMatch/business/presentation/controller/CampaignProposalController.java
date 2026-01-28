package com.example.RealMatch.business.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.business.application.service.CampaignProposalService;
import com.example.RealMatch.business.presentation.docs.CampaignProposalSwagger;
import com.example.RealMatch.business.presentation.dto.request.CampaignProposalRequestDto;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaign Request", description = "캠페인 제안 API")
public class CampaignProposalController implements CampaignProposalSwagger {
    private final CampaignProposalService campaignProposalService;

    @Override
    @PostMapping("/request")
    @Operation(
            summary = "캠페인 제안 생성 API by 박지영",
            description = """
                    크리에이터가 브랜드에 캠페인을 제안합니다.
                    
                    신규 캠페인인 경우 campaignId null 을 보내주세요.
                    기존 캠페인인 경우 campaignId을 보내주세요.
                    
                    기타인 경우 customValue를 포함해서 보내주세요.
                    """
    )
    public CustomResponse<String> requestCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CampaignProposalRequestDto request
    ) {
        campaignProposalService.requestCampaign(userDetails.getUserId(), request);
        return CustomResponse.ok("캠페인 제안에 성공했습니다.");
    }
}

