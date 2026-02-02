package com.example.RealMatch.match.application.service;

import java.util.List;

import com.example.RealMatch.match.domain.entity.enums.CategoryType;
import com.example.RealMatch.match.domain.entity.enums.SortType;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.match.presentation.dto.response.MatchBrandResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchCampaignResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto;

public interface MatchService {

    MatchResponseDto match(Long userId, MatchRequestDto requestDto);

    MatchBrandResponseDto getMatchingBrands(String userId, SortType sortBy, CategoryType category, List<String> tags);

    MatchCampaignResponseDto getMatchingCampaigns(
            String userId,
            String keyword,
            SortType sortBy,
            CategoryType category,
            List<String> tags,
            int page,
            int size
    );
}
