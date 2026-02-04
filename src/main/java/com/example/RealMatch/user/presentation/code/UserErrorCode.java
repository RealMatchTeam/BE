package com.example.RealMatch.user.presentation.code;

import org.springframework.http.HttpStatus;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    // 404 - 조회 실패
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER404_1",
            "존재하지 않는 사용자입니다."
    ),

    PROFILE_CARD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER404_2",
            "내 프로필 카드 정보를 불러오는데 실패하였습니다."
    ),

    SCRAP_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER404_3",
            "내 찜 정보를 불러오는데 실패하였습니다."
    ),

    SOCIAL_INFO_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER404_4",
            "소셜 연동 정보를 불러오는데 실패하였습니다."
    ),

    SOCIAL_CONNECT_FAILED(
            HttpStatus.NOT_FOUND,
            "USER404_5",
            "소셜 계정 연동 추가에 실패하였습니다."
    ),

    SOCIAL_NOT_CONNECTED(
            HttpStatus.NOT_FOUND,
            "USER404_6",
            "연동되지 않은 소셜 계정입니다."
    ),

    SOCIAL_DISCONNECT_FAILED(
            HttpStatus.NOT_FOUND,
            "USER404_7",
            "소셜 계정 연동 해제에 실패하였습니다."
    ),

    BEAUTY_PROFILE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER404_8",
            "뷰티 프로필 정보를 불러오는데 실패하였습니다."
    ),

    FASHION_PROFILE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER404_9",
            "패션 프로필 정보를 불러오는데 실패하였습니다."
    ),

    CONTENT_PROFILE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER404_10",
            "콘텐츠 프로필 정보를 불러오는데 실패하였습니다."
    ),

    USER_MATCHING_DETAIL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER404_11",
            "매칭 상세 정보를 불러오는데 실패하였습니다."
    ),

    // 400 - 요청 오류
    USER_UPDATE_BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            "USER400_1",
            "회원정보 변경에 실패하였습니다. 입력 형식을 확인해주세요."
    ),

    DUPLICATE_NICKNAME(
            HttpStatus.BAD_REQUEST,
            "USER400_2",
            "이미 사용 중인 닉네임입니다."
    ),

    LOGIN_METHOD_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "USER400_3",
            "최소 1개의 로그인 수단은 유지되어야 합니다."
    ),

    TRAIT_UPDATE_FAILED(
            HttpStatus.BAD_REQUEST,
            "USER400_4",
            "특성 정보 수정 중 오류가 발생했습니다. 입력 값을 확인해주세요."
    ),

    WITHDRAW_FAILED(
            HttpStatus.BAD_REQUEST,
            "USER400_5",
            "탈퇴 처리에 실패했습니다. 다시 시도해주세요."
    ),

    INVALID_NICKNAME_FORMAT(
            HttpStatus.BAD_REQUEST,
            "USER400_6",
            "닉네임은 한글, 영문, 숫자만 사용 가능합니다."
    ),

    INVALID_NICKNAME_LENGTH(
            HttpStatus.BAD_REQUEST,
            "USER400_7",
            "닉네임은 2~10자 사이여야 합니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
