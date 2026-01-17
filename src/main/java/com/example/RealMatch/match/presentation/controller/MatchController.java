package com.example.RealMatch.match.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.match.application.service.MatchService;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/brands")
    public CustomResponse<MatchResponseDto> matchBrand(@RequestBody MatchRequestDto requestDto) {
        MatchResponseDto result = matchService.matchBrand(requestDto);
        return CustomResponse.ok(result);
    }
}
