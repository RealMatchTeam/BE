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
import com.example.RealMatch.user.domain.entity.enums.Role;

import io.swagger.v3.oas.annotations.Operation;
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
                    기존 캠페인인 경우 campaignId을 보내주세요.
                    
                    기타인 경우 customValue를 포함해서 보내주세요.
                    
                    태그 ID는 api/v1/tags/content에서 확인할 수 있습니다.
                    """
    )
    public CustomResponse<String> requestCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CampaignProposalRequestDto request
    ) {
        // API 테스트를 위해서 하드코딩 (추후 수정 필요!!)
//        campaignProposalService.requestCampaign(userDetails.getUserId(), request);
        campaignProposalService.requestCampaign(1L, Role.from(userDetails.getRole()), request);
        return CustomResponse.ok("캠페인 제안에 성공했습니다.");
    }

    @GetMapping("/{campaignProposalId}")
    @Operation(
            summary = "캠페인 제안 상세 조회 API",
            description = """
            캠페인 제안 단건의 상세 정보를 조회합니다.
            
            제안에 포함된 콘텐츠 태그 정보와
            제안 상태(REVIEWING / MATCHED / REJECTED)를 함께 반환합니다.
            """
    )
    public CustomResponse<CampaignProposalDetailResponse> getProposalDetail(
            @AuthenticationPrincipal CustomUserDetails principal,
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

