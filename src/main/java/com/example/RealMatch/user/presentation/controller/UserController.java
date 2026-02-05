package com.example.RealMatch.user.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.user.application.service.UserFeatureService;
import com.example.RealMatch.user.application.service.UserService;
import com.example.RealMatch.user.presentation.dto.request.MyEditInfoRequestDto;
import com.example.RealMatch.user.presentation.dto.request.MyFeatureUpdateRequestDto;
import com.example.RealMatch.user.presentation.dto.response.MyEditInfoResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyFeatureResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyLoginResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyPageResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyProfileCardResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyScrapResponseDto;
import com.example.RealMatch.user.presentation.swagger.UserSwagger;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "user", description = "유저 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserSwagger {

    private final UserService userService;
    private final UserFeatureService userFeatureService;

    @Override
    @GetMapping("/me")
    public CustomResponse<MyPageResponseDto> getMyPage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CustomResponse.ok(userService.getMyPage(userDetails.getUserId()));
    }

    @Override
    @GetMapping("/my/profile-card")
    public CustomResponse<MyProfileCardResponseDto> getMyProfileCard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page, // cursor 대신 page (기본값 0)
            @RequestParam(defaultValue = "3") int size  // 이미지 UI에 맞춰 기본값 3으로 변경
    ) {
        return CustomResponse.ok(
                userService.getMyProfileCard(userDetails.getUserId(), page, size)
        );
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

    @Override
    @GetMapping("/me/edit")
    public CustomResponse<MyEditInfoResponseDto> getMyEditInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CustomResponse.ok(userService.getMyEditInfo(userDetails.getUserId()));
    }

    @PostMapping("/me/edit")
    public CustomResponse<Void> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MyEditInfoRequestDto request
    ) {
        userService.updateMyInfo(userDetails.getUserId(), request);
        return CustomResponse.ok(null);
    }

    @Override
    @GetMapping("/me/social-login")
    public CustomResponse<MyLoginResponseDto> getSocialLoginInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CustomResponse.ok(userService.getSocialLoginInfo(userDetails.getUserId()));
    }

    @Override
    @GetMapping("/me/feature")
    public CustomResponse<MyFeatureResponseDto> getMyFeature(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyFeatureResponseDto response = userFeatureService.getMyFeatures(userDetails.getUserId());
        return CustomResponse.ok(response);
    }

    @Override
    @PatchMapping("/me/feature")
    public CustomResponse<Void> updateMyFeature(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MyFeatureUpdateRequestDto request
    ) {
        userFeatureService.updateMyFeatures(userDetails.getUserId(), request);
        return CustomResponse.ok(null);
    }
}
