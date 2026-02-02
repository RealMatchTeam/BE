package com.example.RealMatch.match.presentation.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.match.application.service.MatchService;
import com.example.RealMatch.match.domain.entity.enums.BrandSortType;
import com.example.RealMatch.match.domain.entity.enums.CampaignSortType;
import com.example.RealMatch.match.domain.entity.enums.CategoryType;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.match.presentation.dto.response.MatchBrandResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchCampaignResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto;
import com.example.RealMatch.match.presentation.swagger.MatchSwagger;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
public class MatchController implements MatchSwagger {

    private final MatchService matchService;

    @Override
    @PostMapping
    public CustomResponse<MatchResponseDto> match(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MatchRequestDto requestDto) {
        Long userId = userDetails.getUserId();
        MatchResponseDto result = matchService.match(userId, requestDto);
        return CustomResponse.ok(result);
    }

    @Override
    @GetMapping("/brands")
    public CustomResponse<MatchBrandResponseDto> getMatchingBrands(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "MATCH_SCORE") BrandSortType sortBy,
            @RequestParam(defaultValue = "ALL") CategoryType category,
            @RequestParam(required = false) List<String> tags) {
        String userId = String.valueOf(userDetails.getUserId());
        MatchBrandResponseDto result = matchService.getMatchingBrands(userId, sortBy, category, tags);
        return CustomResponse.ok(result);
    }

    @Override
    @GetMapping("/campaigns")
    public CustomResponse<MatchCampaignResponseDto> getMatchingCampaigns(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "MATCH_SCORE") CampaignSortType sortBy,
            @RequestParam(defaultValue = "ALL") CategoryType category,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String userId = String.valueOf(userDetails.getUserId());
        MatchCampaignResponseDto result = matchService.getMatchingCampaigns(
                userId, keyword, sortBy, category, tags, page, size);
        return CustomResponse.ok(result);
    }
}
