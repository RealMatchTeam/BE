package com.example.RealMatch.match.application.service;

import org.springframework.stereotype.Service;

import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto;

@Service
public class MatchServiceImpl implements MatchService {

    @Override
    public MatchResponseDto matchBrand(MatchRequestDto requestDto) {
        return MatchResponseDto.builder()
                .createrType("민첩")
                .createrBeautyType("미니멀")
                .createrFashionType("미니멀")
                .createrContentsType("미니멀")
                .createrFitBrand("이니스프리")
                .build();
    }
}
