package com.example.RealMatch.business.exception;


import org.springframework.http.HttpStatus;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessErrorCode implements BaseErrorCode {

    // ===== 조회 =====
    CAMPAIGN_APPLY_ALREADY_APPLIED(HttpStatus.BAD_REQUEST, "BUSINESS_CAMPAIGN_APPLY_400_1", "이미 지원한 캠페인입니다."),
    CAMPAIGN_APPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "BUSINESS_CAMPAIGN_APPLY_404_1", "캠페인 지원 내역이 없습니다."),

    CAMPAIGN_PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "BUSINESS_CAMPAIGN_PROPOSAL_404_2", "캠페인 제안 내역이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
