package com.example.RealMatch.business.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.business.application.service.CampaignApplyService;
import com.example.RealMatch.business.presentation.dto.request.CampaignApplyRequest;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name="Business", description = "비즈니스 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/campaigns")
public class CampaignApplyController {

    private final CampaignApplyService campaignApplyService;

    @PostMapping("/{campaignId}/apply")
    public CustomResponse<String> applyCampaign(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Validated @RequestBody CampaignApplyRequest request
    ) {
        campaignApplyService.applyCampaign(
                campaignId,
//                principal.getUserId(),
                // !!! 아래는 임의의 값임, 반드시 위의 주석으로 수정 필요!!!
                1L,
                request.reason()

        );

        return CustomResponse.ok("정상적으로 신청되었습니다.");
    }
}
