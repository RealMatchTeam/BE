package com.example.RealMatch.campaign.exception;

import org.springframework.http.HttpStatus;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CampaignErrorCode implements BaseErrorCode {

    CAMPAIGN_NOT_FOUND(HttpStatus.NOT_FOUND, "CAMPAIGN_404_1", "존재하지 않는 캠페인입니다."),
    CAMPAIGN_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "CAMPAIGN_400_1", "현재 모집 중인 캠페인이 아닙니다."),
    CAMPAIGN_RECRUIT_CLOSED(HttpStatus.BAD_REQUEST, "CAMPAIGN_400_2", "캠페인 모집이 마감되었습니다."),

    CAMPAIGN_LIKE_USER_MISMATCH(HttpStatus.FORBIDDEN, "BUSINESS_CAMPAIGN_LIKE_403_1", "본인의 요청만 처리할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
