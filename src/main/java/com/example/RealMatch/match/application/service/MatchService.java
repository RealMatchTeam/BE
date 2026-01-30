package com.example.RealMatch.match.application.service;

import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.match.presentation.dto.response.MatchBrandResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchCampaignResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto;

public interface MatchService {

    MatchResponseDto match(MatchRequestDto requestDto);

    MatchBrandResponseDto getMatchingBrands(String userId);

    MatchCampaignResponseDto getMatchingCampaigns(String userId);
}
