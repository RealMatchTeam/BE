package com.example.RealMatch.user.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.match.domain.entity.enums.BrandSortType;
import com.example.RealMatch.match.domain.entity.enums.CampaignSortType;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.user.application.service.UserDeleteService;
import com.example.RealMatch.user.application.service.UserFavoriteService;
import com.example.RealMatch.user.application.service.UserFeatureService;
import com.example.RealMatch.user.application.service.UserService;
import com.example.RealMatch.user.application.service.UserWithdrawService;
import com.example.RealMatch.user.presentation.dto.request.MyEditInfoRequestDto;
import com.example.RealMatch.user.presentation.dto.request.MyInstagramUpdateRequestDto;
import com.example.RealMatch.user.presentation.dto.request.MyProfileCardUpdateRequestDto;
import com.example.RealMatch.user.presentation.dto.response.FavoriteBrandListResponseDto;
import com.example.RealMatch.user.presentation.dto.response.FavoriteCampaignListResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyEditInfoResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyFeatureResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyLoginResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyPageResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyProfileCardResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyScrapResponseDto;
import com.example.RealMatch.user.presentation.dto.response.NicknameAvailableResponseDto;
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
    private final UserWithdrawService userWithdrawService;
    private final UserFavoriteService  userFavoriteService;
    private final UserDeleteService userDeleteService;

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
        return CustomResponse.ok(
                userService.getMyProfileCard(userDetails.getUserId())
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
            @RequestBody MatchRequestDto request
    ) {
        userFeatureService.updateMyFeatures(userDetails.getUserId(), request);
        return CustomResponse.ok(null);
    }

    @Override
    @GetMapping("/nickname/available")
    public CustomResponse<NicknameAvailableResponseDto> checkNicknameAvailable(
            @RequestParam String nickname
    ) {
        boolean available = userService.isNicknameAvailable(nickname);
        return CustomResponse.ok(new NicknameAvailableResponseDto(available));
    }

    @Override
    @DeleteMapping("/me")
    public CustomResponse<Void> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userWithdrawService.withdraw(userDetails.getUserId());
        return CustomResponse.ok(null);
    }

    @Override
    @GetMapping("/me/favorites/brand")
    public CustomResponse<FavoriteBrandListResponseDto> getMyFavoriteBrands(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) BrandSortType sort
    ) {
        Long userId = userDetails.getUserId();
        return CustomResponse.ok(
                userFavoriteService.getMyFavoriteBrands(
                        userId,
                        sort == null ? BrandSortType.MATCH_SCORE : sort
                )
        );
    }

    @Override
    @GetMapping("/me/favorites/campaign")
    public CustomResponse<FavoriteCampaignListResponseDto> getMyFavoriteCampaigns(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) CampaignSortType sort
    ) {
        Long userId = userDetails.getUserId();

        return CustomResponse.ok(
                userFavoriteService.getMyFavoriteCampaigns(
                        userId,
                        sort == null ? CampaignSortType.MATCH_SCORE : sort
                )
        );
    }

    @Override
    @PatchMapping("/me/profile-image")
    public CustomResponse<MyProfileCardResponseDto> updateMyProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid MyProfileCardUpdateRequestDto request
    ) {
        return CustomResponse.ok(
                userService.updateMyProfileImage(userDetails.getUserId(), request)
        );
    }

    @Override
    @PatchMapping("/me/instagram")
    public CustomResponse<MyProfileCardResponseDto> updateMySns(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid MyInstagramUpdateRequestDto request
    ) {
        return CustomResponse.ok(
                userService.updateSns(userDetails.getUserId(), request)
        );
    }

    @Override
    @DeleteMapping("/me/delete-immediately")
    public CustomResponse<Void> deleteUserImmediately(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userDeleteService.deleteUserImmediately(userDetails.getUserId());
        return CustomResponse.ok(null);
    }
}
