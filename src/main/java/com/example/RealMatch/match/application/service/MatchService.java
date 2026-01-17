package com.example.RealMatch.match.application.service;

import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto;

public interface MatchService {

    MatchResponseDto matchBrand(MatchRequestDto requestDto);
}
