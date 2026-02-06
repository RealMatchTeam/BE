package com.example.RealMatch.brand.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.brand.application.service.BrandCampaignService;
import com.example.RealMatch.brand.presentation.dto.response.BrandCampaignSliceResponse;
import com.example.RealMatch.brand.presentation.dto.response.BrandExistingCampaignResponse;
import com.example.RealMatch.brand.presentation.dto.response.BrandRecruitingCampaignResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Brand-Campain", description = "브랜드의 캠페인 API")
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandCampaignController {
    private final BrandCampaignService brandCampaignService;


    @Operation(
            summary = "브랜드의 캠페인 내역 조회 API by 박지영",
            description = """
                    브랜드의 캠페인을 조회합니다.   
                    
                    마지막으로 조회된 캠페인의 id를 cursor로 사용하는 페이징 방식입니다.   
                    - 최초 조회 시 cursor 없이 요청합니다.     
                    - 이후 조회 시 응답으로 받은 nextCursor 값을 cursor로 전달합니다.    
                    - 정렬 조건 : UPCOMING /RECRUITING → 모집 시작 날짜 기준, CLOSED → 모집 끝나는 날짜 기준   
                    
                    캠페인 모집 상태는 다음 중 하나로 응답됩니다.   
                    - UPCOMING   : 모집 예정   
                    - CLOSED     : 완료   
                    ❗️ (수정사항 260201 : 진행중은 따로 조회한다고 PM 님께 답변 받아서, 진행 중 상태를 제외했습니다.  ❗️
                    """
    )
    @GetMapping("/{brandId}/campaigns")
    public CustomResponse<BrandCampaignSliceResponse> getBrandCampaigns(
            @Parameter(description = "브랜드 ID", example = "1")
            @PathVariable Long brandId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "5") Integer size
    ) {
        BrandCampaignSliceResponse response = brandCampaignService.getBrandCampaigns(brandId, cursor, size);
        return CustomResponse.ok(response);
    }

    @Operation(
            summary = "브랜드의 기존 캠페인 제안 목록 조회 API by 박지영",
            description = """
                    브랜드의 기존 캠페인 목록을 조회합니다.
                    
                    캠페인 제안(기존 캠페인 선택) 시 사용되는 API입니다.
                    """
    )
    @GetMapping("/{brandId}/existing-campaigns")
    public CustomResponse<BrandExistingCampaignResponse> getExistingCampaigns(
            @Parameter(description = "브랜드 ID", example = "1")
            @PathVariable Long brandId
    ) {
        BrandExistingCampaignResponse response =
                brandCampaignService.getExistingCampaigns(brandId);
        return CustomResponse.ok(response);
    }

    @Operation(
            summary = "브랜드의 진행 중인 캠페인 조회 API by 박지영",
            description = """
                    해당 브랜드의 현재 모집 중인 캠페인 목록을 조회합니다. (Day 남은 일수 포함)
                    
                    - 모집 상태가 RECRUITING 인 캠페인만 조회됩니다.
                    - 브랜드 홈 상단 '진행 중인 캠페인' 영역에 사용됩니다.
                    """
    )
    @GetMapping("/{brandId}/campaigns/recruiting")
    public CustomResponse<BrandRecruitingCampaignResponse> getRecruitingCampaigns(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "브랜드 ID", example = "1")
            @PathVariable Long brandId
    ) {
        BrandRecruitingCampaignResponse response = brandCampaignService.getRecruitingCampaigns(principal.getUserId(), brandId);
        return CustomResponse.ok(response);
    }


}
