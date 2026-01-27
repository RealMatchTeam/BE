package com.example.RealMatch.user.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.match.domain.repository.MatchCampaignHistoryRepository;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.exception.UserException;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.presentation.code.UserErrorCode;
import com.example.RealMatch.user.presentation.dto.response.MyPageResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyProfileCardResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final MatchCampaignHistoryRepository matchCampaignHistoryRepository;

    public MyPageResponseDto getMyPage(Long userId) {
        // 유저 조회 (존재하지 않거나 삭제된 유저 예외 처리)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 매칭 검사 여부 확인 (캠페인 매칭 검사 기록 존재 여부)
        boolean hasMatchingTest = matchCampaignHistoryRepository.existsByUserId(userId);

        // DTO 변환 및 반환
        return MyPageResponseDto.from(user, hasMatchingTest);
    }

    public MyProfileCardResponseDto getMyProfileCard(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 프로필 카드를 볼 자격이 있는지 확인. GUEST 권한이거나, 매칭 테스트 기록이 없다면 예외 발생
        if (user.getRole() == Role.GUEST || !matchCampaignHistoryRepository.existsByUserId(userId)) {
            throw new UserException(UserErrorCode.PROFILE_CARD_NOT_FOUND);
        }

        return new MyProfileCardResponseDto( // 샘플 데이터 반환
                user.getNickname(),
                "FEMALE",
                22,
                List.of("뷰티", "패션"),
                "www.instagram.com/vivi",
                MyProfileCardResponseDto.MatchingResult.sample(),
                MyProfileCardResponseDto.MyType.sample()
        );
    }
}
