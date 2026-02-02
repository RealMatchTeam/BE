package com.example.RealMatch.business.presentation.swagger;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.RealMatch.business.presentation.dto.request.CampaignProposalRequestDto;
import com.example.RealMatch.business.presentation.dto.response.CampaignProposalDetailResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

public interface CampaignProposalSwagger {

    @Operation(
            summary = "캠페인 제안 생성 API by 박지영",
            description = """
                    크리에이터가 브랜드에 캠페인을 제안합니다.
                    
                    신규 캠페인인 경우 campaignId null 을 보내주세요.
                    기존 캠페인인 경우 campaignId을 보내주세요.
                    
                    기타인 경우 customValue를 포함해서 보내주세요.
                    """
    )
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "CampaignProposalRequestExample",
                            summary = "캠페인 제안 요청 예시",
                            value = """
                                    {
                                      "brandId": 1,
                                      "creatorId": 1,
                                      "campaignId": null,
                                      "campaignName": "비플레인 선크림 리뷰 캠페인",
                                      "description": "비플레인 선크림을 체험하고 솔직한 리뷰 콘텐츠를 제작해주세요.",
                                      "formats": [
                                        { "id": 1 }
                                      ],
                                      "categories": [
                                        { "id": 2, "customValue": "성분 분석 리뷰" }
                                      ],
                                      "tones": [
                                        { "id": 3 },
                                        { "id": 4 }
                                      ],
                                      "involvements": [
                                        { "id": 5 }
                                      ],
                                      "usageRanges": [
                                        { "id": 6 }
                                      ],
                                      "rewardAmount": 200000,
                                      "productId": 5,
                                      "startDate": "2025-03-01",
                                      "endDate": "2025-03-15"
                                    }
                                    """
                    )
            )
    )
    CustomResponse<String> createCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CampaignProposalRequestDto request
    );

    @Operation(
            summary = "캠페인 제안 수정(다시 제안하기) API by 박지영",
            description = """
                    내가 제안했던 것을 다시 제안하는 API 입니다.    
                    /api/v1/campaigns/proposal/request에서 생성했던 제안을 수정해서 다시 제안할 때, 해당 API를 사용해주세요.   
                    
                    campaignProposalId는 /api/v1/campaigns/collaborations/me에서 확인해주세요.     
                    (masterJWT로 조회 불가능 API, 크리에이터/브랜드 계정으로 로그인 필요)
                    """
    )
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "CampaignProposalRequestExample",
                            summary = "캠페인 수정 요청 예시",
                            value = """
                                    {
                                      "brandId": 1,
                                      "creatorId": 1,
                                      "campaignId": null,
                                      "campaignName": "비플레인 선크림 리뷰 캠페인",
                                      "description": "비플레인 선크림을 체험하고 솔직한 리뷰 콘텐츠를 제작해주세요.",
                                      "formats": [
                                        { "id": 1 }
                                      ],
                                      "categories": [
                                        { "id": 2, "customValue": "성분 분석 리뷰" },
                                        { "id": 7 }
                                      ],
                                      "tones": [
                                        { "id": 3 }
                                      ],
                                      "involvements": [
                                        { "id": 5 }
                                      ],
                                      "usageRanges": [
                                        { "id": 6 }
                                      ],
                                      "rewardAmount": 100000,
                                      "productId": 5,
                                      "startDate": "2025-03-29",
                                      "endDate": "2025-04-01"
                                    }
                                    """
                    )
            )
    )
    CustomResponse<String> modifyCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "캠페인 Proposal ID", example = "1")
            @PathVariable Long campaignProposalID,
            @org.springframework.web.bind.annotation.RequestBody @Valid CampaignProposalRequestDto request
    );

    @Operation(
            summary = "캠페인 제안 상세 조회 API by 박지영",
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
    CustomResponse<CampaignProposalDetailResponse> getProposalDetail(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "캠페인 Proposal ID", example = "1")
            @PathVariable Long campaignProposalId
    );

}
