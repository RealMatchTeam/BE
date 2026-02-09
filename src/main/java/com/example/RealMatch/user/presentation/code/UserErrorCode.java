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

    SCRAP_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER404_2",
            "내 찜 정보를 불러오는데 실패하였습니다."
    ),

    SOCIAL_INFO_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER404_3",
            "소셜 연동 정보를 불러오는데 실패하였습니다."
    ),

    USER_MATCHING_DETAIL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER404_4",
            "매칭 상세 정보를 불러오는데 실패하였습니다."
    ),


    DUPLICATE_NICKNAME(
            HttpStatus.BAD_REQUEST,
            "USER400_1",
            "이미 사용 중인 닉네임입니다."
    ),

    TRAIT_UPDATE_FAILED(
            HttpStatus.BAD_REQUEST,
            "USER400_2",
            "특성 정보 수정 중 오류가 발생했습니다. 입력 값을 확인해주세요."
    ),

    INVALID_NICKNAME_FORMAT(
            HttpStatus.BAD_REQUEST,
            "USER400_3",
            "닉네임은 한글, 영문, 숫자만 사용 가능합니다."
    ),

    INVALID_NICKNAME_LENGTH(
            HttpStatus.BAD_REQUEST,
            "USER400_4",
            "닉네임은 2~10자 사이여야 합니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
