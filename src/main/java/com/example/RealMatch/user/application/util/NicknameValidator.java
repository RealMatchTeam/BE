package com.example.RealMatch.user.application.util;

import org.springframework.stereotype.Component;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.presentation.code.UserErrorCode;

import lombok.RequiredArgsConstructor;
/**
 * 닉네임 검증을 위한 공통 유틸리티 클래스
 * - 형식, 길이, 중복 검증을 한 곳에서 처리
 */
@Component
@RequiredArgsConstructor
public class NicknameValidator {

    private final UserRepository userRepository;

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 10;
    private static final String NICKNAME_PATTERN = "^[가-힣a-zA-Z0-9]+$";

    /**
     * 닉네임 사용 가능 여부 확인 (중복 체크만)
     * - 형식/길이 검증 후 중복 여부 반환
     * - 검증 실패 시 예외 발생
     *
     * @param nickname 검증할 닉네임
     * @return 사용 가능하면 true, 중복이면 false
     * @throws CustomException 형식/길이가 잘못된 경우
     */
    public boolean isAvailable(String nickname) {
        validateFormat(nickname);
        return !userRepository.existsByNickname(nickname.trim());
    }

    /**
     * 닉네임 검증 (형식, 길이, 중복 모두 체크)
     * - 검증 실패 시 예외 발생
     *
     * @param nickname 검증할 닉네임
     * @throws CustomException 검증 실패 시
     */
    public void validate(String nickname) {
        validateFormat(nickname);
        validateDuplicate(nickname);
    }

    /**
     * 닉네임 변경 시 검증 (기존 닉네임과 비교)
     * - 기존 닉네임과 같으면 검증 통과
     * - 다르면 형식, 길이, 중복 체크
     *
     * @param newNickname 새 닉네임
     * @param currentNickname 현재 닉네임
     * @throws CustomException 검증 실패 시
     */
    public void validateForUpdate(String newNickname, String currentNickname) {
        // 기존 닉네임과 동일하면 검증 통과
        if (newNickname.equals(currentNickname)) {
            return;
        }

        // 형식 검증
        validateFormat(newNickname);

        // 중복 검증
        validateDuplicate(newNickname);
    }

    /**
     * 닉네임 형식 및 길이 검증
     */
    private void validateFormat(String nickname) {
        // null 체크
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new CustomException(UserErrorCode.INVALID_NICKNAME_FORMAT);
        }

        String trimmedNickname = nickname.trim();

        // 길이 체크 (2~10자)
        int length = trimmedNickname.codePointCount(0, trimmedNickname.length());
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new CustomException(UserErrorCode.INVALID_NICKNAME_LENGTH);
        }

        // 형식 체크 (한글, 영문, 숫자만)
        if (!trimmedNickname.matches(NICKNAME_PATTERN)) {
            throw new CustomException(UserErrorCode.INVALID_NICKNAME_FORMAT);
        }
    }

    /**
     * 닉네임 중복 검증
     */
    private void validateDuplicate(String nickname) {
        if (userRepository.existsByNickname(nickname.trim())) {
            throw new CustomException(UserErrorCode.DUPLICATE_NICKNAME);
        }
    }
}
