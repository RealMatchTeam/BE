package com.example.RealMatch.user.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.user.application.service.UserService;
import com.example.RealMatch.user.presentation.dto.response.MyPageResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyProfileCardResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyScrapResponseDto;
import com.example.RealMatch.user.presentation.swagger.UserSwagger;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "user", description = "유저 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserSwagger {

    private final UserService userService;

    @Override
    @GetMapping("/me")
    public CustomResponse<MyPageResponseDto> getMyPage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CustomResponse.ok(userService.getMyPage(userDetails.getUserId()));
    }

    @Override
    @GetMapping("/me/profile-card")
    public CustomResponse<MyProfileCardResponseDto> getMyProfileCard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CustomResponse.ok(userService.getMyProfileCard(userDetails.getUserId()));
    }

    @Override
    @GetMapping("/me/scrap")
    public CustomResponse<MyScrapResponseDto> getMyScrap(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String type,
            @RequestParam(required = false, defaultValue = "matchingRate") String sort
    ) {
        return CustomResponse.ok(userService.getMyScrap(userDetails.getUserId(), type, sort));
    }
}
