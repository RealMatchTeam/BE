package com.example.RealMatch.business.presentation.docs;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.example.RealMatch.business.presentation.dto.request.CampaignProposalRequestDto;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
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
                                      "campaignId": null,
                                      "campaignName": "비플레인 선크림 리뷰 캠페인",
                                      "description": "비플레인 선크림을 체험하고 솔직한 리뷰 콘텐츠를 제작해주세요.",
                                                                  "formats": [
                                                                    { "id": "32000000-0000-0000-0000-000000000000" }
                                                                  ],
                                                                  "categories": [
                                                                    { "id": "31310000-0000-0000-0000-000000000000", "customValue": "성분 분석 리뷰" }
                                                                  ],
                                                                  "tones": [
                                                                    { "id": "31360000-0000-0000-0000-000000000000" },
                                                                    { "id": "31330000-0000-0000-0000-000000000000" }
                                                                  ],
                                                                  "involvements": [
                                                                    { "id": "32320000-0000-0000-0000-000000000000" }
                                                                  ],
                                                                  "usageRanges": [
                                                                    { "id": "32350000-0000-0000-0000-000000000000" }
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
    CustomResponse<String> requestCampaignProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CampaignProposalRequestDto request
    );
}
