package com.example.RealMatch.match.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.match.application.service.MatchService;
import com.example.RealMatch.match.domain.entity.enums.CategoryType;
import com.example.RealMatch.match.domain.entity.enums.SortType;
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
    public CustomResponse<MatchResponseDto> match(@RequestBody MatchRequestDto requestDto) {
        MatchResponseDto result = matchService.match(requestDto);
        return CustomResponse.ok(result);
    }

    @Override
    @GetMapping("/brands/{userId}")
    public CustomResponse<MatchBrandResponseDto> getMatchingBrands(
            @PathVariable String userId,
            @RequestParam(defaultValue = "MATCH_SCORE") SortType sortBy,
            @RequestParam(defaultValue = "ALL") CategoryType category,
            @RequestParam(required = false) List<String> tags) {
        MatchBrandResponseDto result = matchService.getMatchingBrands(userId, sortBy, category, tags);
        return CustomResponse.ok(result);
    }

    @Override
    @GetMapping("/campaigns/{userId}")
    public CustomResponse<MatchCampaignResponseDto> getMatchingCampaigns(
            @PathVariable String userId,
            @RequestParam(defaultValue = "MATCH_SCORE") SortType sortBy,
            @RequestParam(defaultValue = "ALL") CategoryType category,
            @RequestParam(required = false) List<String> tags) {
        MatchCampaignResponseDto result = matchService.getMatchingCampaigns(userId, sortBy, category, tags);
        return CustomResponse.ok(result);
    }
}
