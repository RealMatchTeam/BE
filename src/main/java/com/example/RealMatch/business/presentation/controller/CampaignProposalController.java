package com.example.RealMatch.business.presentation.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.business.application.service.CampaignProposalQueryService;
import com.example.RealMatch.business.application.service.CampaignProposalService;
import com.example.RealMatch.business.presentation.docs.CampaignProposalSwagger;
import com.example.RealMatch.business.presentation.dto.request.CampaignProposalRequestDto;
import com.example.RealMatch.business.presentation.dto.response.CampaignProposalDetailResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(
            summary = "캠페인 제안 생성 API by 박지영",
            description = """
                    크리에이터가 브랜드에 캠페인을 제안합니다.
                    
                    신규 캠페인인 경우 campaignId null을 보내주세요.
                    기존 캠페인인 경우 campaignId을 보내주세요 + campaignName도 기존 캠페인의 campaignName 값을 넣어주세요.
                    
                    기타인 경우 customValue를 포함해서 보내주세요.
                    
                    태그 ID는 api/v1/tags/content에서 확인할 수 있습니다.
                    """
    )
    public CustomResponse<String> createCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CampaignProposalRequestDto request
    ) {
        campaignProposalService.createCampaignProposal(userDetails,  request);
        return CustomResponse.ok("캠페인 제안에 성공했습니다.");
    }

    @Override
    @PostMapping("/{campaignProposalID}/re-request")
    @Operation(
            summary = "캠페인 제안 수정(다시 제안하기) API by 박지영",
            description = """
                    내가 제안했던 것을 다시 제안하는 API 입니다.    
                    /api/v1/campaigns/proposal/request에서 생성했던 제안을 수정해서 다시 제안할 때, 해당 API를 사용해주세요.   
                    
                    campaignProposalId는 /api/v1/campaigns/collaborations/me에서 확인해주세요.     
                    (masterJWT로 조회 불가능 API, 크리에이터/브랜드 계정으로 로그인 필요)
                    """
    )
    public CustomResponse<String> modifyCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "캠페인 Proposal ID", example = "43bf11cd-b075-4187-b43b-07ee5cd54d41")
            @PathVariable UUID campaignProposalID,
            @RequestBody @Valid CampaignProposalRequestDto request
    ) {
        campaignProposalService.modifyCampaignProposal(userDetails, campaignProposalID, request);
        return CustomResponse.ok("캠페인 제안을 수정했습니다.");
    }

    @GetMapping("/{campaignProposalId}")
    @Operation(
            summary = "켐페인 제안 상세 조회 API by 박지영",
            description = """
                    한 건의 캠페인 제안 상세 정보를 조회합니다.
                    
                    campaignProposalId는 /api/v1/campaigns/collaborations/me에서 확인해주세요.    
                    (masterJWT로 조회 불가능 API, 크리에이터/브랜드 계정으로 로그인 필요)
                    
                    <태그>   
                    formats : 형식    
                    categories : 종류    
                    tones : 톤   
                    involvements : 관여도    
                    usageRanges : 활용 범위    
                    """
    )
    public CustomResponse<CampaignProposalDetailResponse> getProposalDetail(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "캠페인 Proposal ID", example = "43bf11cd-b075-4187-b43b-07ee5cd54d41")
            @PathVariable UUID campaignProposalId
    ) {
        CampaignProposalDetailResponse response =
                campaignProposalQueryService.getProposalDetail(
                        principal.getUserId(),
                        campaignProposalId
                );

        return CustomResponse.ok(response);
    }
}

