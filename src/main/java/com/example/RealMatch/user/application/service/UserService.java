package com.example.RealMatch.user.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.match.domain.repository.MatchCampaignHistoryRepository;
import com.example.RealMatch.user.application.util.NicknameValidator;
import com.example.RealMatch.user.domain.entity.AuthenticationMethod;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.UserContentCategory;
import com.example.RealMatch.user.domain.entity.UserMatchingDetail;
import com.example.RealMatch.user.domain.entity.enums.AuthProvider;
import com.example.RealMatch.user.domain.repository.AuthenticationMethodRepository;
import com.example.RealMatch.user.domain.repository.UserContentCategoryRepository;
import com.example.RealMatch.user.domain.repository.UserMatchingDetailRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.infrastructure.ScrapMockDataProvider;
import com.example.RealMatch.user.presentation.code.UserErrorCode;
import com.example.RealMatch.user.presentation.dto.request.MyEditInfoRequestDto;
import com.example.RealMatch.user.presentation.dto.request.MyInstagramUpdateRequestDto;
import com.example.RealMatch.user.presentation.dto.request.MyProfileCardUpdateRequestDto;
import com.example.RealMatch.user.presentation.dto.response.MyEditInfoResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyLoginResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyPageResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyProfileCardResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyScrapResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final MatchCampaignHistoryRepository matchCampaignHistoryRepository;
    private final ScrapMockDataProvider scrapMockDataProvider;
    private final AuthenticationMethodRepository authenticationMethodRepository;
    private final UserMatchingDetailRepository userMatchingDetailRepository;
    private final UserContentCategoryRepository userContentCategoryRepository;
    private final NicknameValidator nicknameValidator;

    public MyPageResponseDto getMyPage(Long userId) {
        // 유저 조회 (존재하지 않거나 삭제된 유저 예외 처리)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 매칭 검사 여부 확인 (캠페인 매칭 검사 기록 존재 여부)
        boolean hasMatchingTest = matchCampaignHistoryRepository.existsByUserId(userId);

        // DTO 변환 및 반환
        return MyPageResponseDto.from(user, hasMatchingTest);
    }

    public MyProfileCardResponseDto getMyProfileCard(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 매칭 검사 진행 여부 예외 처리 - 매칭 검사를 안하면 프로필 카드가 없음
        UserMatchingDetail detail = userMatchingDetailRepository
                .findByUserIdAndIsDeprecatedFalse(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.PROFILE_CARD_NOT_FOUND));

        List<UserContentCategory> categories =
                userContentCategoryRepository.findByUserId(userId);

        return MyProfileCardResponseDto.from(user, detail, categories);
    }

    public MyScrapResponseDto getMyScrap(Long userId, String type, String sort) {

        // 매칭 검사 진행 여부 예외 처리 - 매칭 검사를 안하면 찜한 내역이 없음
        if (!userMatchingDetailRepository.existsByUserIdAndIsDeprecatedFalse(userId)) {
            throw new CustomException(UserErrorCode.SCRAP_NOT_FOUND);
        }

        // 찜 타입이 null일 때 예외 처리
        if (type == null) {
            throw new CustomException(UserErrorCode.SCRAP_NOT_FOUND);
        }

        // 찜 타입에 따른 분기 처리
        return switch (type.toLowerCase()) {
            case "brand" -> MyScrapResponseDto.ofBrandType(
                    scrapMockDataProvider.getBrandScraps(sort, null)
            );
            case "campaign" -> MyScrapResponseDto.ofCampaignType(
                    scrapMockDataProvider.getCampaignScraps(sort, null)
            );
            default -> throw new CustomException(UserErrorCode.SCRAP_NOT_FOUND);
        };
    }

    public MyEditInfoResponseDto getMyEditInfo(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 해당 유저의 모든 소셜 로그인 방법 조회
        List<AuthProvider> providers = authenticationMethodRepository.findByUserId(userId)
                .stream()
                .map(AuthenticationMethod::getProvider)
                .toList();

        // 유저 로그인 정보를 불러오지 못했을 때
        if (providers.isEmpty()) {
            throw new CustomException(UserErrorCode.SOCIAL_INFO_NOT_FOUND);
        }

        // DTO 변환 및 반환
        return MyEditInfoResponseDto.from(user, providers);
    }

    @Transactional
    public void updateMyInfo(Long userId, MyEditInfoRequestDto request) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // NicknameValidator의 validateForUpdate 사용
        nicknameValidator.validateForUpdate(request.nickname(), user.getNickname());

        // 정보 수정
        user.updateInfo(
                request.nickname(),
                request.address(),
                request.detailAddress()
        );
    }

    public MyLoginResponseDto getSocialLoginInfo(Long userId) {
        // 유저 조회
        if (!userRepository.existsById(userId)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        // 해당 유저의 모든 소셜 로그인 방법 조회
        List<AuthProvider> linkedProviders = authenticationMethodRepository.findByUserId(userId)
                .stream()
                .map(AuthenticationMethod::getProvider)
                .toList();

        // DTO 변환 및 반환
        return MyLoginResponseDto.from(linkedProviders);
    }

    @Transactional
    public MyProfileCardResponseDto updateMyProfileImage(
            Long userId,
            MyProfileCardUpdateRequestDto request
    ) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 매칭 검사 진행 여부 예외 처리 - 매칭 검사를 안하면 프로필 카드가 없음
        UserMatchingDetail detail = userMatchingDetailRepository
                .findByUserIdAndIsDeprecatedFalse(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.PROFILE_CARD_NOT_FOUND));

        // 이미지 URL만 교체
        user.updateProfileImage(request.getProfileImageUrl());

        // 변경된 프로필 이미지로 프로필 카드 DTO 재생성
        List<UserContentCategory> categories =
                userContentCategoryRepository.findByUserId(userId);

        return MyProfileCardResponseDto.from(user, detail, categories);
    }

    // 인스타 아이디 수정
    @Transactional
    public MyProfileCardResponseDto updateSns(
            Long userId,
            MyInstagramUpdateRequestDto request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        UserMatchingDetail detail = userMatchingDetailRepository
                .findByUserIdAndIsDeprecatedFalse(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.PROFILE_CARD_NOT_FOUND));

        detail.updateSns(request.getSnsAccount());

        List<UserContentCategory> categories =
                userContentCategoryRepository.findByUserId(userId);

        return MyProfileCardResponseDto.from(user, detail, categories);
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return nicknameValidator.isAvailable(nickname);
    }
}
