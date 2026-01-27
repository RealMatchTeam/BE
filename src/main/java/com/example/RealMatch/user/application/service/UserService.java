package com.example.RealMatch.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.match.domain.repository.MatchingTestRepository;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.exception.UserException;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.presentation.code.UserErrorCode;
import com.example.RealMatch.user.presentation.dto.response.MyPageResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final MatchingTestRepository matchingTestRepository;

    public MyPageResponseDto getMyPage(Long userId) {
        // 1. 유저 조회 (존재하지 않거나 삭제된 유저 예외 처리)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 2. 매칭 검사 여부 확인
        boolean hasMatchingTest = matchingTestRepository.existsByUserId(userId);

        // 3. DTO 변환 및 반환
        return MyPageResponseDto.from(user, hasMatchingTest);
    }
}
